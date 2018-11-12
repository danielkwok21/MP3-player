package com.example.danie.mp3player.Utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class Util {
    public static void Toast(Context c, String s){
        Toast toast = Toast.makeText(c, s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.show();
    }
}
