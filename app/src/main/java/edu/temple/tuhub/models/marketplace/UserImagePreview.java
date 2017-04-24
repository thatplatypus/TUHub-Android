package edu.temple.tuhub.models.marketplace;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.temple.tuhub.R;

/**
 * Created by Ben on 4/6/2017.
 */

public class UserImagePreview extends LinearLayout {

    @BindView(R.id.user_image_preview)
    ImageView userImage;

    @BindView(R.id.user_image_preview_delete)
    ImageView deleteButton;

    private boolean isFromS3 = false;
    private S3DeleteListener deleteListener;
    private String s3FileKey;


    public UserImagePreview(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView =  inflater.inflate(R.layout.user_image_preview, this);

        ButterKnife.bind(this, rootView);

        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFromS3){
                    deleteListener.addToDeleteList(s3FileKey);
                }
                ((ViewGroup)UserImagePreview.this.getParent()).removeView(UserImagePreview.this);
            }
        });

    }

    public void setS3DeleteListener(S3DeleteListener deleteListener){
        this.deleteListener = deleteListener;
    }

    public void setS3FileKey(String s3FileKey){
        this.s3FileKey = s3FileKey;
    }

    public void setIsFromS3(boolean isFromS3){
        this.isFromS3 = isFromS3;
    }

    public boolean isFromS3(){
        return isFromS3;
    }

    public void setImageBitmap(Bitmap bitmap){
        userImage.setImageBitmap(bitmap);
    }

    public ImageView getUserImage(){
        return userImage;
    }

    public void rotateImage(float degrees){
        userImage.setRotation(degrees);
    }

    public void removeDeleteButton(){
        deleteButton.setVisibility(View.INVISIBLE);
        deleteButton.setClickable(false);
    }

    public interface S3DeleteListener{
        void addToDeleteList(String fileKey);
    }

}
