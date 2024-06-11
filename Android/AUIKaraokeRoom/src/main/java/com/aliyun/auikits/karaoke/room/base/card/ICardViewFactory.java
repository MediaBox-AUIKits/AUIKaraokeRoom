package com.aliyun.auikits.karaoke.room.base.card;

import android.content.Context;
import android.view.ViewGroup;

public interface ICardViewFactory {
    void registerCardView(String cardType, Class clazz);
    BaseCard createCardView(Context context, ViewGroup parent, String cardType);
}
