/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.ImageTargets;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.vuforia.Device;
import com.vuforia.ImageTarget;
import com.vuforia.ImageTargetResult;
import com.vuforia.Matrix44F;
import com.vuforia.Renderer;
import com.vuforia.State;
import com.vuforia.Tool;
import com.vuforia.Trackable;
import com.vuforia.TrackableResult;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleAppRenderer;
import com.vuforia.samples.SampleApplication.SampleAppRendererControl;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.CubeObject;
import com.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.Model;
import com.vuforia.samples.SampleApplication.utils.SampleApplication3DModel;
import com.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.vuforia.samples.SampleApplication.utils.Teapot;
import com.vuforia.samples.SampleApplication.utils.Texture;
import com.vuforia.samples.VuforiaSamples.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


// The renderer class for the ImageTargets_1 sample.
public class ImageTargetRenderer implements GLSurfaceView.Renderer, SampleAppRendererControl
{
    private static final String LOGTAG = "ImageTargetRenderer_1";
    
    private SampleApplicationSession vuforiaAppSession;
    private ImageTargets mActivity;
    private SampleAppRenderer mSampleAppRenderer;

    private Vector<Texture> mTextures;
    
    private int shaderProgramID;
    private int vertexHandle;
    private int textureCoordHandle;
    private int mvpMatrixHandle;
    private int opacityHandle;
    private int colorHandle;
    private int texSampler2DHandle;

    FloatBuffer mVertexBuffer;
    FloatBuffer mTextureBuffer;
    FloatBuffer mNormalBuffer;
    ShortBuffer mIndices;

    private Teapot mTeapot;
    private CubeObject mCubeObject;

    private Renderer mRenderer;
    
    private float kBuildingScale = 0.012f;
    private SampleApplication3DModel mBuildingsModel;

    private boolean mIsActive = false;
    private boolean mModelIsLoaded = false;
    
    private static final float OBJECT_SCALE_FLOAT = 0.003f;

//    public int numFaces;
//    public FloatBuffer positions;
//    public FloatBuffer normals;
//    public FloatBuffer textureCoordinates;


//    public final class ObjLoader{
//
//        public final int numFaces;
//        Context context;
//        public final float[] normals;
//        public final float[] textureCoordinates;
//        public final float[] positions;
//
//        public ObjLoader(Context activity, int file){
//            Vector<Float> vertices = new Vector<>();
//            Vector<Float> normals = new Vector<>();
//            Vector<Float> textures = new Vector<>();
//            Vector<String> faces = new Vector<>();
//            BufferedReader reader = null;
//            this.context = activity;
//            try {
//                InputStreamReader in = new InputStreamReader(context.getResources().openRawResource(file));
//                reader = new BufferedReader(in);
//
//                String line;
//                while ((line = reader.readLine())!=null){
//                    String[] parts = line.split(" ");
//                    switch (parts[0]){
//                        case "v":
//                            float x = Float.valueOf(parts[2]);
//                            vertices.add(x);
//                            vertices.add(Float.valueOf(parts[3]));
//                            vertices.add(Float.valueOf(parts[4]));
//                            break;
//                        case "vt":
//                            textures.add(Float.valueOf(parts[2]));
//                            textures.add(Float.valueOf(parts[3]));
//                            break;
//                        case "vn":
//                            normals.add(Float.valueOf(parts[2]));
//                            normals.add(Float.valueOf(parts[3]));
//                            normals.add(Float.valueOf(parts[4]));
//                            break;
//                        case "f":
//                            faces.add(parts[1]);
//                            faces.add(parts[3]);
//                            faces.add(parts[5]);
//                            break;
//                    }
//                }
//            }catch (IOException e){
//            }finally {
//                if(reader!=null){
//                    try {
//                        reader.close();
//                    }catch (IOException e){
//                    }
//                }
//            }
//            numFaces = faces.size();
//            this.normals = new float[numFaces * 3];
//            textureCoordinates = new float[numFaces * 2];
//            positions = new float[numFaces * 3];
//            int positionIndex = 0;
//            int normalIndex = 0;
//            int textureIndex = 0;
//            for (String face : faces){
//                String[] parts = face.split("/");
//
//                int index = 3 * (Integer.valueOf(parts[0]) - 1);
//                positions[positionIndex++] = vertices.get(index++);
//                positions[positionIndex++] = vertices.get(index++);
//                positions[positionIndex++] = vertices.get(index);
//
//                index = 2 * (Integer.valueOf(parts[1]) - 1);
//                textureCoordinates[normalIndex++] = textures.get(index++);
//                textureCoordinates[normalIndex++] = 1 - textures.get(index);
//
//                index = 3 * (Integer.valueOf(parts[2]) - 1);
//                this.normals[textureIndex++] = normals.get(index++);
//                this.normals[textureIndex++] = normals.get(index++);
//                this.normals[textureIndex++] = normals.get(index);
//            }
//        }
//    }

    
    public ImageTargetRenderer(ImageTargets activity, SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;
        // SampleAppRenderer used to encapsulate the use of RenderingPrimitives setting
        // the device mode AR/VR and stereo mode
        mSampleAppRenderer = new SampleAppRenderer(this, mActivity, Device.MODE.MODE_AR, false, 0.01f , 5f);
    }
    
    
    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;
        
        // Call our function to render content from SampleAppRenderer class
        mSampleAppRenderer.render();
    }


    public void setActive(boolean active)
    {
        mIsActive = active;

        if(mIsActive)
            mSampleAppRenderer.configureVideoBackground();
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        
        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();

        mSampleAppRenderer.onSurfaceCreated();
    }
    
    
    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        
        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);

        // RenderingPrimitives to be updated when some rendering change is done
        mSampleAppRenderer.onConfigurationChanged(mIsActive);

        initRendering();
    }
    
    
    // Function for initializing the renderer.
    private void initRendering()
    {
//        ObjLoader objLoader = new ObjLoader(mActivity, R.raw.alvais_ff_obj );
//        numFaces = objLoader.numFaces;
//
//        positions = ByteBuffer.allocateDirect(objLoader.positions.length * 4)
//        .order(ByteOrder.nativeOrder()).asFloatBuffer();
//        positions.put(objLoader.positions).position(0);
//
//        normals = ByteBuffer.allocateDirect(objLoader.normals.length * 4)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer();
//        normals.put(objLoader.normals).position(0);
//
//        textureCoordinates = ByteBuffer.allocateDirect(objLoader.textureCoordinates.length * 4)
//                .order(ByteOrder.nativeOrder()).asFloatBuffer();
//        textureCoordinates.put(objLoader.textureCoordinates).position(0);

        mCubeObject = new CubeObject();
        mRenderer = Renderer.getInstance();
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
            CubeShaders.CUBE_MESH_VERTEX_SHADER,
            CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexPosition");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
            "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "modelViewProjectionMatrix");
        opacityHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "opacity");
        colorHandle = GLES20.glGetUniformLocation(shaderProgramID, "color");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
            "texSampler2D");

//        if(!mModelIsLoaded) {
//            mTeapot = new Teapot();
//
//            try {
//
////                Model model = new Model(R.raw.alvais_ff_obj, mActivity);
////                mVertexBuffer = model.getVertices();
////                mTextureBuffer = model.getTexCoords();
////                mNormalBuffer = model.getNormals();
////                mIndices = model.getIndices();
//
//                mBuildingsModel = new SampleApplication3DModel();
//                mBuildingsModel.loadModel(mActivity.getResources().getAssets(),
//                        "ImageTargets/Buildings.txt");
//                mModelIsLoaded = true;
//            } catch (IOException e) {
//                Log.e(LOGTAG, "Unable to load buildings");
//            }

            // Hide the Loading Dialog
            mActivity.loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
//        }
//
    }

    public void updateConfiguration()
    {
        mSampleAppRenderer.onConfigurationChanged(mIsActive);
    }

    // The render function called from SampleAppRendering by using RenderingPrimitives views.
    // The state is owned by SampleAppRenderer which is controlling it's lifecycle.
    // State should not be cached outside this method.
    public void renderFrame(State state, float[] projectionMatrix)
    {
        boolean gotImage = false;
        int id = -1;
        // Renders video background replacing Renderer.DrawVideoBackground()
        mSampleAppRenderer.renderVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        // Did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {

            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);

            if (!result.isOfType(ImageTargetResult.getClassType()))
                continue;

            ImageTarget imageTarget = (ImageTarget) trackable;

            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());
            float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

//            int textureIndex = trackable.getName().equalsIgnoreCase("stones") ? 0
//                    : 1;
//            textureIndex = trackable.getName().equalsIgnoreCase("tarmac") ? 2
//                    : textureIndex;

            // deal with the modelview and projection matrices
            float[] modelViewProjection = new float[16];
            id = imageTarget.getId();

            float[] imageSize = imageTarget.getSize().getData();
//            Matrix.translateM(modelViewMatrix, 0, imageSize[0]/2, imageSize[2]/2,
//                    imageSize[1]/2);
                Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
                Matrix.scaleM(modelViewMatrix, 0, imageSize[0]/2, imageSize[2]/2, imageSize[1]/2);
            Matrix.multiplyMM(modelViewProjection, 0, projectionMatrix, 0, modelViewMatrix, 0);

            // activate the shader program and bind the vertex/normal/tex coords
            GLES20.glUseProgram(shaderProgramID);

                //GLES20.glDisable(GLES20.GL_CULL_FACE);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                        false, 0, mCubeObject.getVertices());

            GLES20.glUniform1f(opacityHandle, 0.3f);
            GLES20.glUniform3f(colorHandle, 0.0f, 0.0f, 0.0f);
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mCubeObject.getTexCoords());
                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);

                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                        mTextures.get(0).mTextureID[0]);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                        modelViewProjection, 0);
                GLES20.glUniform1i(texSampler2DHandle, 0);

                gotImage = true;

            //finally render
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                    mCubeObject.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                    mCubeObject.getIndices());

            //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,positions.limit());
            // disable the enabled arrays
            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);

            SampleUtils.checkGLError("Render Frame");

        }

        if(gotImage)
            mActivity.showMenu(id);


        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mRenderer.end();
    }

    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }
    
    
    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;
        
    }
    
}
