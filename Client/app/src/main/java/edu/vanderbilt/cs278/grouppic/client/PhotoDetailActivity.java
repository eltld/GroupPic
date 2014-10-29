package edu.vanderbilt.cs278.grouppic.client;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.Callable;
import org.magnum.videoup.client.R;

import java.util.ArrayList;
import java.util.Collection;

import butterknife.ButterKnife;
import butterknife.InjectView;
import edu.vanderbilt.cs278.grouppic.repository.Comment;
import edu.vanderbilt.cs278.grouppic.repository.Picture;


public class PhotoDetailActivity extends Activity {

    ArrayList<Comment> comments;
    ArrayList<String> content;
    ArrayAdapter<String> listViewAdapter;

    private long currentPicture_;

    @InjectView(R.id.comment_list)
    protected ListView commentList_;

    @InjectView(R.id.detail_imageView)
    protected ImageView img_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        ButterKnife.inject(this);

        img_ = (ImageView) findViewById(R.id.detail_imageView);

        img_.setImageResource(R.drawable.android_wallpaper);

        content = new ArrayList<String>();

        listViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, content);

        commentList_.setAdapter(listViewAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.photo_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getCurrentPicture() {
        final PictureSvcApi svc = PictureSvc.getOrShowLogin(this);

        if (svc != null) {
            CallableTask.invoke(new Callable<Picture>() {
                @Override
                public Picture call() throws Exception {
                    return svc.getPictureWithId(currentPicture_);
                }
            }, new TaskCallback<Picture>() {
                @Override
                public void success(Picture result) {
                    byte[] img = result.getImage();
                    Bitmap bmp = BitmapFactory.decodeByteArray(img, 0 , img.length);

                    img_.setImageBitmap(bmp);
                }

                @Override
                public void error(Exception e) {

                }
            });
        }
    }

    private void refreshMessages() {
        final PictureSvcApi svc = PictureSvc.getOrShowLogin(this);

        if (svc != null) {
            CallableTask.invoke(new Callable<Collection<Comment>>() {
                                    @Override
                                    public Collection<Comment> call() throws Exception {
                                        return svc.getComments(currentPicture_);
                                    }
                                }, new TaskCallback<Collection<Comment>>() {
                                    @Override
                                    public void success(Collection<Comment> result) {
                                        for (Comment c : result) {
                                            content.add(c.getUser());
                                        }
                                    }

                                    @Override
                                    public void error(Exception e) {

                                        Toast.makeText(
                                                PhotoDetailActivity.this,
                                                "Unable to fetch the video list, please login again.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

            );
        }
    }
}
