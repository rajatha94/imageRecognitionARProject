package com.vuforia.samples.SampleApplication.utils;

import android.content.Context;
import android.hardware.camera2.params.Face;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by SESA431244 on 12-06-2017.
 */

public class Model {

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int SHORT_SIZE_BYTES = 2;
    private FloatBuffer _vb;
    private FloatBuffer _nb;
    private FloatBuffer _tcb;
    private ShortBuffer _ib;

    private short[] indices;

    private float[] tempV;
    private float[] tempVt;
    private float[] tempVn;

    private ArrayList<Vector3D> vertices;
    private ArrayList<Vector3D> vertexTexture;
    private ArrayList<Vector3D> vertexNormal;
    private ArrayList<Face> faces;
    private int vertexCount;

    private ArrayList<GroupObject> groupObjects;

    class Vector3D{
        float x,y,z;
        public Vector3D(float parseFloat, float parseFloat2, float parseFloat3){
            x = parseFloat;
            y = parseFloat2;
            z = parseFloat3;
        }
        public float getX(){
            return x;
        }
        public float getY(){
            return y;
        }
        public float getZ(){
            return z;
        }

    }

    class Face {
        public ArrayList<Vector3D> getVertices(){
            return vertices;
        }
        public ArrayList<Vector3D> getUvws(){
            return vertexTexture;
        }
        public ArrayList<Vector3D> getNormals(){
            return vertexNormal;
        }
    }

    class GroupObject{
        String objectName;

        public void setObjectName(String string){
            this.objectName = string;
        }
        public String getObjectName(){
            return objectName;
        }
    }
    private Context context;
    private int modelID;

    public Model(int modelID, Context activity){

        this.vertices = new ArrayList<Vector3D>();
        this.vertexTexture = new ArrayList<Vector3D>();
        this.vertexNormal = new ArrayList<Vector3D>();
        this.faces = new ArrayList<Face>();

        this.groupObjects = new ArrayList<GroupObject>();

        this.modelID = modelID;
        this.context = activity;

        loadFile();
    }

    private int loadFile(){
        InputStream inputStream = context.getResources().openRawResource(modelID);
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        try{
            loadOBJ(in);
            Log.d("LOADING FILE", "FILE LOADED SUCCESSFULLY!!!!!!!");
        }catch (IOException e){
            e.printStackTrace();
        }
        return 1;
    }

    private void loadOBJ(BufferedReader in) throws IOException{
        Log.d("LOADING FILE", "STARTING!!.......");
        GroupObject defaultObject = new GroupObject();
        GroupObject currentObject = defaultObject;

        this.groupObjects.add(defaultObject);
        String Line;
        String[] Blocks;
        String CommandBlock;

        while ((Line = in.readLine())!=null){
            Blocks = Line.split("");
            CommandBlock = Blocks[0];
            if (CommandBlock.equals("g")){
                if(Blocks[1] == "default")
                    currentObject = defaultObject;
                else {
                    GroupObject groupObject = new GroupObject();
                    groupObject.setObjectName(Blocks[1]);
                    currentObject = groupObject;
                    groupObjects.add(groupObject);
                }
            }
            if (CommandBlock.equals("v")){
                Vector3D vertex = new Vector3D(Float.parseFloat(Blocks[1]),
                        Float.parseFloat(Blocks[2]),
                        Float.parseFloat(Blocks[3]));
                this.vertices.add(vertex);
            }
            if(CommandBlock.equals("vt")){
                Vector3D vertexTex = new Vector3D(Float.parseFloat(Blocks[1]),
                        Float.parseFloat(Blocks[2]), 0.0f);
                this.vertexTexture.add(vertexTex);
            }
            if(CommandBlock.equals("vn")){
                Vector3D vertexNorm = new Vector3D(Float.parseFloat(Blocks[1]),
                        Float.parseFloat(Blocks[2]), Float.parseFloat(Blocks[3]));
                this.vertexNormal.add(vertexNorm);
            }
            if(CommandBlock.equals("f")){
                Face face = new Face();
                faces.add(face);

                String[] faceParams;
                for (int i= 1;i < Blocks.length; i++){
                    faceParams = Blocks[i].split("/");

                    face.getVertices().add(this.vertices.get(Integer
                    .parseInt(faceParams[0]) - 1));

                    if(faceParams[1] == ""){

                    } else {
                        face.getUvws().add(this.vertexTexture.get(Integer.
                                parseInt(faceParams[1]) - 1));
                        face.getNormals().add(
                                this.vertexNormal.get(Integer
                                .parseInt(faceParams[2]) - 1));
                    }
                }
            }
        }
        fillInBuffersWithNormals();

        Log.d("OBJ OBJECT DATA", "V = "+ vertices.size() + " VN = "
        + vertexTexture.size() + " VT = " + vertexNormal.size());
    }


    private void fillInBuffers() {

        int facesSize = faces.size();

        vertexCount = facesSize * 3;

        tempV = new float[facesSize * 3 * 3];
        tempVt = new float[facesSize * 2 * 3];
        indices = new short[facesSize * 3];

        for (int i = 0; i < facesSize; i++) {
            Face face = faces.get(i);
            tempV[i * 9] = face.getVertices().get(0).getX();
            tempV[i * 9 + 1] = face.getVertices().get(0).getY();
            tempV[i * 9 + 2] = face.getVertices().get(0).getZ();
            tempV[i * 9 + 3] = face.getVertices().get(1).getX();
            tempV[i * 9 + 4] = face.getVertices().get(1).getY();
            tempV[i * 9 + 5] = face.getVertices().get(1).getZ();
            tempV[i * 9 + 6] = face.getVertices().get(2).getX();
            tempV[i * 9 + 7] = face.getVertices().get(2).getY();
            tempV[i * 9 + 8] = face.getVertices().get(2).getZ();
            tempVt[i * 6] = face.getUvws().get(0).getX();
            tempVt[i * 6 + 1] = face.getUvws().get(0).getY();
            tempVt[i * 6 + 2] = face.getUvws().get(1).getX();
            tempVt[i * 6 + 3] = face.getUvws().get(1).getY();
            tempVt[i * 6 + 4] = face.getUvws().get(2).getX();
            tempVt[i * 6 + 5] = face.getUvws().get(2).getY();
            indices[i * 3] = (short) (i * 3);
            indices[i * 3 + 1] = (short) (i * 3 + 1);
            indices[i * 3 + 2] = (short) (i * 3 + 2);
        }

        _vb = ByteBuffer.allocateDirect(tempV.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        _vb.put(tempV);
        _vb.position(0);

        _tcb = ByteBuffer.allocateDirect(tempVt.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        _tcb.put(tempVt);
        _tcb.position(0);

        _ib = ByteBuffer.allocateDirect(indices.length * SHORT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        _ib.put(indices);
        _ib.position(0);
    }

    private void fillInBuffersWithNormals() {

        int facesSize = faces.size();

        vertexCount = facesSize * 3;

        tempV = new float[facesSize * 3 * 3];
        tempVt = new float[facesSize * 2 * 3];
        tempVn = new float[facesSize * 3 * 3];
        indices = new short[facesSize * 3];

        for (int i = 0; i < facesSize; i++) {
            Face face = faces.get(i);
            tempV[i * 9] = face.getVertices().get(0).getX();
            tempV[i * 9 + 1] = face.getVertices().get(0).getY();
            tempV[i * 9 + 2] = face.getVertices().get(0).getZ();
            tempV[i * 9 + 3] = face.getVertices().get(1).getX();
            tempV[i * 9 + 4] = face.getVertices().get(1).getY();
            tempV[i * 9 + 5] = face.getVertices().get(1).getZ();
            tempV[i * 9 + 6] = face.getVertices().get(2).getX();
            tempV[i * 9 + 7] = face.getVertices().get(2).getY();
            tempV[i * 9 + 8] = face.getVertices().get(2).getZ();

            tempVn[i * 9] = face.getNormals().get(0).getX();
            tempVn[i * 9 + 1] = face.getNormals().get(0).getY();
            tempVn[i * 9 + 2] = face.getNormals().get(0).getZ();
            tempVn[i * 9 + 3] = face.getNormals().get(1).getX();
            tempVn[i * 9 + 4] = face.getNormals().get(1).getY();
            tempVn[i * 9 + 5] = face.getNormals().get(1).getZ();
            tempVn[i * 9 + 6] = face.getNormals().get(2).getX();
            tempVn[i * 9 + 7] = face.getNormals().get(2).getY();
            tempVn[i * 9 + 8] = face.getNormals().get(2).getZ();

            tempVt[i * 6] = face.getUvws().get(0).getX();
            tempVt[i * 6 + 1] = face.getUvws().get(0).getY();
            tempVt[i * 6 + 2] = face.getUvws().get(1).getX();
            tempVt[i * 6 + 3] = face.getUvws().get(1).getY();
            tempVt[i * 6 + 4] = face.getUvws().get(2).getX();
            tempVt[i * 6 + 5] = face.getUvws().get(2).getY();

            indices[i * 3] = (short) (i * 3);
            indices[i * 3 + 1] = (short) (i * 3 + 1);
            indices[i * 3 + 2] = (short) (i * 3 + 2);
        }

        _vb = ByteBuffer.allocateDirect(tempV.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        _vb.put(tempV);
        _vb.position(0);

        _tcb = ByteBuffer.allocateDirect(tempVt.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        _tcb.put(tempVt);
        _tcb.position(0);

        _nb = ByteBuffer.allocateDirect(tempVn.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        _nb.put(tempVn);
        _nb.position(0);

        _ib = ByteBuffer.allocateDirect(indices.length * SHORT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        _ib.put(indices);
        _ib.position(0);
    }

    public FloatBuffer getVertices(){
        return _vb;
    }

    public FloatBuffer getTexCoords(){
        return _tcb;
    }

    public FloatBuffer getNormals(){
        return _nb;
    }

    public ShortBuffer getIndices(){
        return _ib;
    }

}
