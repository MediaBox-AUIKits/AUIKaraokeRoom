package com.aliyun.auikits.karaoke.room.vm;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

public class KtvScoreDialogViewModel extends ViewModel {
    public ObservableField<String> scoreText = new ObservableField<>();

    public void bind(int score) {
        scoreText.set(String.valueOf(score));
    }
}
