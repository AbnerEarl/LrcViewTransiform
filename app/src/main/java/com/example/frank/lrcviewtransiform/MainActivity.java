package com.example.frank.lrcviewtransiform;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.frank.lrcviewtransiform.view.ILrcBuilder;
import com.example.frank.lrcviewtransiform.view.ILrcView;
import com.example.frank.lrcviewtransiform.view.ILrcViewListener;
import com.example.frank.lrcviewtransiform.view.impl.DefaultLrcBuilder;
import com.example.frank.lrcviewtransiform.view.impl.LrcRow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public final static String TAG = "MainActivity";

    //自定义LrcView，用来展示歌词
    ILrcView mLrcView;
    //更新歌词的频率，每秒更新一次
    private int mPalyTimerDuration = 1000;
    //更新歌词的定时器
    private Timer mTimer;
    //更新歌词的定时任务
    private TimerTask mTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLrcView=(ILrcView)findViewById(R.id.lrcView);

        //从assets目录下读取歌词文件内容
        String lrc = getFromAssets("ttt.lrc");
        //解析歌词构造器
        ILrcBuilder builder = new DefaultLrcBuilder();
        //解析歌词返回LrcRow集合
        List<LrcRow> rows = builder.getLrcRows(lrc);
        //将得到的歌词集合传给mLrcView用来展示
        mLrcView.setLrc(rows);

        //开始播放歌曲并同步展示歌词
        beginLrcPlay();

        //设置自定义的LrcView上下拖动歌词时监听
        mLrcView.setListener(new ILrcViewListener() {
            //当歌词被用户上下拖动的时候回调该方法,从高亮的那一句歌词开始播放
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (mPlayer != null) {
                    Log.d(TAG, "onLrcSeeked:" + row.time);
                    mPlayer.seekTo((int) row.time);
                }
            }
        });



    }









    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    /**
     * 从assets目录下读取歌词文件内容
     * @param fileName
     * @return
     */
    public String getFromAssets(String fileName){
        try {
            InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String result="";
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals(""))
                    continue;
                result += line + "\r\n";
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    MediaPlayer mPlayer;

    /**
     * 开始播放歌曲并同步展示歌词
     */
    public void beginLrcPlay(){
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(getAssets().openFd("ttt.mp3").getFileDescriptor());
            //准备播放歌曲监听
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                //准备完毕
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    if(mTimer == null){
                        mTimer = new Timer();
                        mTask = new LrcTask();
                        mTimer.scheduleAtFixedRate(mTask, 0, mPalyTimerDuration);
                    }
                }
            });
            //歌曲播放完毕监听
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    stopLrcPlay();
                }
            });
            //准备播放歌曲
            mPlayer.prepare();
            //开始播放歌曲
            mPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 停止展示歌曲
     */
    public void stopLrcPlay(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 展示歌曲的定时任务
     */
    class LrcTask extends TimerTask{
        @Override
        public void run() {
            //获取歌曲播放的位置
            final long timePassed = mPlayer.getCurrentPosition();
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    //滚动歌词
                    mLrcView.seekLrcToTime(timePassed);
                }
            });

        }
    };








}
