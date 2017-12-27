package com.example.enrico.myapplication;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDb;
    private FirebaseUser mCurrentUser;

    //
    private CircleImageView mDisplayImage;
    private TextView mDisplayName;
    private TextView mStatus;
    private Button mChangeStatus;
    private Button mChangePicture;
    private RSACryptography mRsa = new RSACryptography();

    private static final int GALLERY_PICK = 1;

    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage = (CircleImageView) findViewById(R.id.pic_settings);
        mDisplayName = (TextView) findViewById(R.id.settings_displayname);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mChangeStatus = (Button) findViewById(R.id.settings_changestatus);
        mChangePicture = (Button) findViewById(R.id.settings_changepicture);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();

        mUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        mUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                byte[] encrypted_name = name.getBytes();
                String DECRYPTED_name = mRsa.decrypt(encrypted_name).toString();
                Toast.makeText(SettingsActivity.this,name,Toast.LENGTH_LONG).show();
                String img = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_img = dataSnapshot.child("thumb_image").getValue().toString();
                String username = dataSnapshot.child("username").getValue().toString();

                mDisplayName.setText(name);
                mStatus.setText(status);
                Picasso.with(SettingsActivity.this).load(img).into(mDisplayImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = mStatus.getText().toString();

                Intent statIntent = new Intent(SettingsActivity.this,StatusActivity.class);
                statIntent.putExtra("status_value",status);
                startActivity(statIntent);
            }
        });

        mChangePicture.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(View v) {
                /*CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);*/
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        //to get image uri after cropped
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data); // the cropped result
            if (resultCode == RESULT_OK) {
                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading image");
                mProgressDialog.setMessage("Please wait while we upload and process the image");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                Uri resultUri = result.getUri(); // to get the uri
                String uid = mCurrentUser.getUid();
                StorageReference filepath = mImageStorage.child("profile_images").child(uid+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            String download_URL = task.getResult().getDownloadUrl().toString();
                            mUserDb.child("image").setValue(download_URL).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mProgressDialog.dismiss();
                                    } else {

                                    }
                                }
                            });

                        } else {
                            Toast.makeText(SettingsActivity.this,"Fail uploading image",Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();

                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public static String randomGenerator(){
        Random generator = new Random();
        StringBuilder randomString = new StringBuilder();
        int randomLength = generator.nextInt(12);
        char temp;
        for(int i=0;i<randomLength;i++){
            temp = (char)(generator.nextInt(96)+32);
            randomString.append(temp);
        }
        return randomString.toString();
    }
}
