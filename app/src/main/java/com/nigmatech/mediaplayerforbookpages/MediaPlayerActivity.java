package com.nigmatech.mediaplayerforbookpages;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MediaPlayerActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {


    int width, height, width_original;

    // تعريفات متغيرات هنستقبل عليها اسم الملف من الاكتفتي السابق
    Bundle extras;
    String getMediaNumberClicked;
    int positionClicked;
    //--------------------

    PowerManager pm;

    AnimationDrawable animationDrawable, mAnimation;

    // تعريف متغيرات المصفوفات علشان نعمل التتابع في التشغيل
    String[] mediaFileName;
    String[] mediaFileNameStringArray;
    TypedArray soraName;

    // ListView الحاصة بصفحات السورة
    ListView soraListView;

    // بداية التعريفات للمتغيرات
    SeekBar seek_bar;
    //----------------------------
    ImageButton button_Play, button_volume_mute, button_volume_up, button_volume_down, button_btnStop, button_skip_next, button_skip_previous, button_forward_10, button_replay_10;
    ImageView image_Rhythm;
    TextView current_time, sound_duration;
    MediaPlayer mediaPlayer;

    Handler mHandler = new Handler();
    Utilities utils;


    private final static int MAX_VOLUME = 100; //int for max vol
    int length;
    int releaseMute = 1;
    int currVolume = 90; // current vol that app start with it
    //--------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        // تحميل المتغيرات و استقبال اسم الملف
        extras = getIntent().getExtras();
        if (extras != null) {
            getMediaNumberClicked = extras.getString("mediaNumberClicked");
            positionClicked = extras.getInt("positionClicked");
            //The key argument here must match that used in the other activity
        }
        //---------------------------


        mediaPlayer = new MediaPlayer();

        // تعديل الاكشن بار الاسم و التصميم
        processing_actionBar();

        linking_elements();

        utils = new Utilities();
        seek_bar.setOnSeekBarChangeListener(this);
        seek_bar.setMax(mediaPlayer.getDuration());


        playSong();

        playButton();
        muteButton();
        volumeUpButton();
        volumeDownButton();
        stopButton();
        skipNextButton();
        skipPreviousButton();
        replay10Button();
        forward10Button();

        zoomInOut();

        showPlayer();

    }

    private void showPlayer() {
        final Button showPlayerButton = findViewById(R.id.showBtn);

        final LinearLayout mediaPlayerlinearLayout = findViewById(R.id.mediaPlayer);

        showPlayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayerlinearLayout.getVisibility() == View.VISIBLE) {
                    // Its visible
                    mediaPlayerlinearLayout.setVisibility(View.GONE);
                    showPlayerButton.setText("إظهار مشغل الصوت");
                } else {
                    // Either gone or invisible
                    mediaPlayerlinearLayout.setVisibility(View.VISIBLE);
                    showPlayerButton.setText("إخفاء مشغل الصوت");
                }
            }
        });

    }

    private void zoomInOut() {

        final LinearLayout layout = findViewById(R.id.layout);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                width = layout.getMeasuredWidth();
                height = layout.getMeasuredHeight();
                width_original = width;
            }
        });

        final ImageButton imageButtonZoomIn = findViewById(R.id.zoomIn);
        final ImageButton imageButtonZoomOut = findViewById(R.id.zoomOut);

        final ListView mListView = findViewById(R.id.soraListViewPages);
        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mListView.getLayoutParams();

        Log.i("zoom ", "width : " + width + " height : " + height + " width_original : " + width_original);
        imageButtonZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (width < width_original * 1.5) {
                    width = (int) (width * 1.25);
                    height = (int) (height * 1.5);
                    lp.width = width;
                    lp.height = height;
                    mListView.setLayoutParams(lp);

                    imageButtonZoomOut.setEnabled(true);
                    imageButtonZoomOut.setClickable(true);
                    imageButtonZoomOut.setAlpha(1f);

                    Log.i("zoom ", "width : " + width + " height : " + height + " width_original : " + width_original);
                }
                if (width > width_original * 1.5) {
                    imageButtonZoomIn.setEnabled(false);
                    imageButtonZoomIn.setClickable(false);
                    imageButtonZoomIn.setAlpha(0.25f);
                    Log.i("zoom ", "width : " + width + " height : " + height + " width_original : " + width_original);
                }
            }
        });

        imageButtonZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (width > width_original) {
                    width = (int) (width / 1.25);
                    height = (int) (height / 1.5);
                    lp.width = width;
                    lp.height = height;
                    mListView.setLayoutParams(lp);

                    imageButtonZoomIn.setEnabled(true);
                    imageButtonZoomIn.setClickable(true);
                    imageButtonZoomIn.setAlpha(1f);

                    Log.i("zoom ", "width : " + width + " hight : " + height + " width_original : " + width_original);
                }
                if (width <= width_original) {
                    imageButtonZoomOut.setEnabled(false);
                    imageButtonZoomOut.setClickable(false);
                    imageButtonZoomOut.setAlpha(0.25f);
                }
            }
        });
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {

            long totalDuration = mediaPlayer.getDuration();
            long currentDuration = mediaPlayer.getCurrentPosition();

            sound_duration.setText(String.format("%s", utils.milliSecondsToTimer(totalDuration)));
            current_time.setText(String.format("%s", utils.milliSecondsToTimer(currentDuration)));

            int progress = (utils.getProgressPercentage(currentDuration, totalDuration));
            seek_bar.setProgress(progress);

            mHandler.postDelayed(this, 100);
        }
    };


    // تعديل الاكشن بار الاسم و التصميم ميثود
    public void processing_actionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.custom_actionbar);

        TextView mTitleTextView = findViewById(R.id.title_text);
        mTitleTextView.setText(getIntent().getExtras().getString("mediaFileNameClicked"));
    }

    public void linking_elements() {
        seek_bar = findViewById(R.id.seekBar);
        current_time = findViewById(R.id.songCurrentDurationLabel1);
        sound_duration = findViewById(R.id.songTotalDurationLabel);

        button_Play = findViewById(R.id.btnPlay);
        button_volume_mute = findViewById(R.id.volume_mute);
        button_volume_up = findViewById(R.id.volume_up);
        button_volume_down = findViewById(R.id.volume_down);
        button_btnStop = findViewById(R.id.btnStop);
        button_skip_next = findViewById(R.id.skip_next);
        button_skip_previous = findViewById(R.id.skip_previous);
        button_forward_10 = findViewById(R.id.forward_10);
        button_replay_10 = findViewById(R.id.replay_10);

        image_Rhythm = findViewById(R.id.img_equilizer);
        image_Rhythm.setBackgroundResource(R.drawable.simple_animation);
        mAnimation = (AnimationDrawable) image_Rhythm.getBackground();
    }

    public void playSong() {
        try {
            //تحضير مصفوفة اسماء الاغنية و اسماء ملف الاغنية
            mediaFileName = getResources().getStringArray(R.array.audioFileName);
            mediaFileNameStringArray = getResources().getStringArray(R.array.audioNameString);

            soraViewPages();

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }

            mediaPlayer.reset();

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier(getMediaNumberClicked,
                    "raw", getPackageName())));

            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();

                    image_Rhythm.setBackgroundResource(R.drawable.simple_animation);
                    mAnimation = (AnimationDrawable) image_Rhythm.getBackground();

//                    Toast.makeText(MediaPlayerActivity.this, mediaFileNameStringArray[positionClicked], Toast.LENGTH_SHORT).show();
                    startRhythm();
                    updateProgressBar();
                    button_Play.setImageResource(R.drawable.ic_pause_black);
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playNext();
                }
            });

            button_Play.setImageResource(R.drawable.ic_pause_black);

            seek_bar.setProgress(0);
            seek_bar.setMax(100);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //اضافة صورة لصفحات كل سورة يتم تلاوتها
    private void soraViewPages() {
        // تحضير مصفوفة صفحات الصورة
        soraName = getResources().obtainTypedArray(getResources().getIdentifier(getMediaNumberClicked, "array", getPackageName()));

        int count = soraName.length();
        int[] imageArray = new int[count];

        for (int i = 0; i < soraName.length(); i++) {
            imageArray[i] = soraName.getResourceId(i, 0);
        }
//        Toast.makeText(this, "count : " + count, Toast.LENGTH_SHORT).show();
        // Each row in the list stores country name, currency and flag
        List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();

        //for Each تعبي مصفوفة اسماء السور في مصفوفة تستخدم في الادابتر
        for (int value : imageArray) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("soraPages", Integer.toString(value));
            aList.add(hm);
        }

        // Keys used in Hashmap
        String[] from = {"soraPages"};

        // Ids of views in listview_layout
        int[] to = {R.id.soraImageView};

        // Instantiating an adapter to store each items
        // R.layout.sora_pages_list_view defines the layout of each item
        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.sora_pages_list_view, from, to);

        // Getting a reference to listview of main.xml layout file
        soraListView = findViewById(R.id.soraListViewPages);

        // Setting the adapter to the listView
        soraListView.setAdapter(adapter);

    }
//////////////////////////////////////////////


    // برمجة زر التشغيل والإيقاف
    private void playButton() {
        button_Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayerPause();
                    } else if (!mediaPlayer.isPlaying()) {
                        mediaPlayerResume();
                    }
                } else {
                    playSong();
                }
            }
        });
    }
    //---------------------------------

    // برمجة زر Mute UnMute ايقاف الصوت و تشغيل الصوت
    private void muteButton() {
        button_volume_mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (releaseMute == 1) {
                    mute();
                } else {
                    unMute();
                }
            }
        });
    }
    //---------------------------------

    // برمجة زر Volume Up رفع الصوت
    private void volumeUpButton() {
        button_volume_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volumeUp();
            }
        });
    }
    //---------------------------------

    // برمجة زر Volume Down رفع الصوت
    private void volumeDownButton() {
        button_volume_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volumeDown();
            }
        });
    }
    //---------------------------------

    // برمجة زر STOP رفع الصوت
    private void stopButton() {
        button_btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayer();
            }
        });
    }
    //---------------------------------

    // برمجة زر skipNextButton الانتقال للسورة القادمة
    private void skipNextButton() {
        button_skip_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });
    }
    //---------------------------------

    // برمجة زر skipPreviousButton الانتقال للسورة السابقة
    private void skipPreviousButton() {
        button_skip_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skip_previous();
            }
        });
    }
    //---------------------------------


    private void playNext() {

        if (positionClicked == mediaFileName.length - 1) {
            positionClicked = 0;
        } else {
            positionClicked++;
        }

        //هنشغل هنا التتابع للاغاني بعد ما تخلص
        getMediaNumberClicked = mediaFileName[positionClicked];

        TextView mTitleTextView = findViewById(R.id.title_text);
        mTitleTextView.setText(mediaFileNameStringArray[positionClicked]);
        Toast.makeText(MediaPlayerActivity.this, mediaFileNameStringArray[positionClicked], Toast.LENGTH_SHORT).show();

        if (mediaPlayer != null) {
            mediaPlayer.reset();

            try {
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier(getMediaNumberClicked,
                        "raw", getPackageName())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
        } else {
            playSong();
        }
        soraViewPages();

    }

    private void stopRhythm() {
        image_Rhythm.post(new Runnable() {
            public void run() {
                mAnimation.stop();
            }
        });
    }

    private void startRhythm() {
        image_Rhythm.post(new Runnable() {
            public void run() {
                mAnimation.start();
            }
        });
    }

    public void updateProgressBar() {
        if (mediaPlayer != null) {
            mHandler.postDelayed(mUpdateTimeTask, 100);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration;
        if (mediaPlayer != null) {
            totalDuration = mediaPlayer.getDuration();
            int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

            mediaPlayer.seekTo(currentPosition);

            updateProgressBar();
        }
    }


    private void replay10Button() {
        button_replay_10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replay10();
            }
        });
    }

    private void replay10() {

        long totalDuration = 0;
        long currentDuration = 0;
        if (mediaPlayer != null) {
            currentDuration = mediaPlayer.getCurrentPosition();
            totalDuration = mediaPlayer.getDuration();

            if ((currentDuration - 10000) >= 0) {

                int progress = (utils.getProgressPercentage(currentDuration, totalDuration));
                seek_bar.setProgress(progress);
                mediaPlayer.seekTo((int) (currentDuration - 10000));
                updateProgressBar();
//                Toast.makeText(getApplicationContext(), "You have Jumped backward 10 seconds", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "اعادة 10 ثوان", Toast.LENGTH_SHORT).show();

            } else {
//                Toast.makeText(getApplicationContext(), "Cannot jump backward 10 seconds", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "لا يمكن الاعادة الان", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void forward10Button() {
        button_forward_10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forward10();
            }
        });

    }

    private void forward10() {
        long totalDuration = 0;
        long currentDuration = 0;
        if (mediaPlayer != null) {
            currentDuration = mediaPlayer.getCurrentPosition();
            totalDuration = mediaPlayer.getDuration();

            if ((currentDuration + 10000) <= totalDuration) {

                int progress = (utils.getProgressPercentage(currentDuration, totalDuration));
                seek_bar.setProgress(progress);
                mediaPlayer.seekTo((int) (currentDuration + 10000));
                updateProgressBar();
//                Toast.makeText(getApplicationContext(), "You have Jumped forward 10 seconds", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "التقدم 10 ثوان", Toast.LENGTH_SHORT).show();

            } else {
//                Toast.makeText(getApplicationContext(), "Cannot jump forward 10 seconds", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "لا يمكن التقديم الان", Toast.LENGTH_SHORT).show();

            }
        }
    }

    // setVolume 0 to mute Vol
    private void mute() {
        if (mediaPlayer != null) {
            if (releaseMute == 1) {
                mediaPlayer.setVolume(0, 0);
//                Toast.makeText(this, "MUTE", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "تم كتم الصوت", Toast.LENGTH_SHORT).show();
                releaseMute = 0;
                button_volume_mute.setBackgroundColor(Color.parseColor("#666bff"));
            }
        }
    }

    private void unMute() {
        if (mediaPlayer != null) {
            if (releaseMute == 0) {
                setVolumeControl();
//                Toast.makeText(this, "release MUTE", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "تشغيل الصوت", Toast.LENGTH_SHORT).show();
                releaseMute = 1;
                button_volume_mute.setBackgroundResource(android.R.drawable.btn_default);
            }
        }
    }


    //setVolume increment to volUp
    private void volumeUp() {

        if (currVolume < 100) {
            currVolume += 10;
            setVolumeControl();
        } else {
//            Toast.makeText(this, "VOLUME is MAX", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "مستوى الصوت أعلى ما يمكن", Toast.LENGTH_SHORT).show();
        }
        if (releaseMute == 0) {
            unMute();
        }
    }

    //setVolume decrement to volDown
    private void volumeDown() {
        if (currVolume > 0) {
            currVolume -= 10;
            setVolumeControl();
        } else {
//            Toast.makeText(this, "VOLUME is " + currVolume, Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "مستوى الصوت أقل ما يمكن" , Toast.LENGTH_SHORT).show();
        }
        if (releaseMute == 0) {
            unMute();
        }
    }

    private void setVolumeControl() {
        if (mediaPlayer != null) {
            if (currVolume != 100) {
                float volume = (float) (1 - (Math.log(MAX_VOLUME - currVolume) / Math.log(MAX_VOLUME)));
                mediaPlayer.setVolume(volume, volume);
            } else {
                mediaPlayer.setVolume(1, 1);
            }

        }
    }


    private void stopPlayer() {
        if (mediaPlayer != null) {
            mHandler.removeCallbacks(mUpdateTimeTask);
            seek_bar.setProgress(0);
            stopRhythm();
            image_Rhythm.setBackgroundResource(R.drawable.image_0);
            button_Play.setImageResource(R.drawable.ic_play_arrow_black);
            animationDrawable.stop();

            long currentDuration = 0;
            current_time.setText(String.format("%s", utils.milliSecondsToTimer(currentDuration)));

            releaseMediaPlayer();
//            Toast.makeText(this, "MediaPlayer released", Toast.LENGTH_SHORT).show();
        }
    }

    private void skip_previous() {
        if (positionClicked == 0) {
            positionClicked = mediaFileName.length - 1;

        } else {
            positionClicked--;
        }

        //هنشغل هنا التتابع للاغاني بعد ما تخلص
        getMediaNumberClicked = mediaFileName[positionClicked];

        TextView mTitleTextView = findViewById(R.id.title_text);
        mTitleTextView.setText(mediaFileNameStringArray[positionClicked]);
        Toast.makeText(MediaPlayerActivity.this, mediaFileNameStringArray[positionClicked], Toast.LENGTH_SHORT).show();
        if (mediaPlayer != null) {
            mediaPlayer.reset();

            try {
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://" + getPackageName() + "/" + getResources().getIdentifier(getMediaNumberClicked,
                        "raw", getPackageName())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
        } else {
            playSong();
        }
        soraViewPages();
    }

    private void mediaPlayerPause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            stopRhythm();
            button_Play.setImageResource(R.drawable.ic_play_arrow_black);
            animationDrawable.stop();
        }
    }

    private void mediaPlayerResume() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            startRhythm();
            button_Play.setImageResource(R.drawable.ic_pause_black);
            animationDrawable.start();
        }
    }

    @Override
    protected void onRestart() {
        // when the screen is about to turn off
        // Use the PowerManager to see if the screen is turning off
        if (!pm.isScreenOn()) {
            // this is the case when onPause() is called by the system due to the screen turning off
        } else {
            // this is when onPause() is called when the screen has not turned off
            mediaPlayerResume();
        }
        super.onRestart();
    }

    @Override
    protected void onStop() {
        if (mediaPlayer != null) {
            mediaPlayerPause();
            length = mediaPlayer.getCurrentPosition();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    @Override
    public void onBackPressed() {
        stopPlayer();
        finish();
        startActivity(new Intent(getBaseContext(), ReadAndListenActivity.class));
    }

    /**
     * Clean up the media player by releasing its resources.
     */
    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mediaPlayer != null) {

            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mediaPlayer.release();

            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mediaPlayer = null;

//            Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
        }

    }
}
