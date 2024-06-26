package com.aliyun.auikits.karaoke.room.vm;


import android.view.View;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.aliyun.auikits.karaoke.room.model.entity.ChatMusicItem;

public class ChatMusicCardViewModel extends ViewModel {

    public ObservableField<String> title = new ObservableField<String>("");
    public ObservableField<String> author = new ObservableField<String>("0");
    public ObservableBoolean playing = new ObservableBoolean(false);

    public void bind(ChatMusicItem musicItem) {
        this.title.set(musicItem.getTitle());
        this.author.set(musicItem.getAuthor());
        this.playing.set(musicItem.isPlaying());
    }


    public void onPlayOrStopMusic(View view) {
        this.playing.set(!this.playing.get());
    }
}
