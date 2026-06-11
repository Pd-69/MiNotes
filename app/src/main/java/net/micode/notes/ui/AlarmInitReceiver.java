/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;


/**
 * 闹钟初始化广播接收器
 * 用于在系统启动时重新设置所有未过期的闹钟
 */
public class AlarmInitReceiver extends BroadcastReceiver {

    // 查询投影，指定需要从数据库获取的列
    private static final String [] PROJECTION = new String [] {
        NoteColumns.ID,        // 笔记ID
        NoteColumns.ALERTED_DATE  // 提醒日期
    };

    // 列索引常量
    private static final int COLUMN_ID                = 0;        // ID列索引
    private static final int COLUMN_ALERTED_DATE      = 1;        // 提醒日期列索引

    /**
     * 接收广播时调用
     * 1. 获取当前时间
     * 2. 查询数据库中所有未过期的笔记提醒
     * 3. 为每个未过期的提醒重新设置闹钟
     * @param context 上下文
     * @param intent 接收到的广播意图
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 获取当前时间
        long currentDate = System.currentTimeMillis();
        
        // 查询数据库中所有未过期的笔记提醒
        Cursor c = context.getContentResolver().query(Notes.CONTENT_NOTE_URI,
                PROJECTION,
                NoteColumns.ALERTED_DATE + ">? AND " + NoteColumns.TYPE + "=" + Notes.TYPE_NOTE,
                new String[] { String.valueOf(currentDate) },
                null);

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    // 获取提醒日期
                    long alertDate = c.getLong(COLUMN_ALERTED_DATE);
                    
                    // 创建闹钟接收器的意图
                    Intent sender = new Intent(context, AlarmReceiver.class);
                    sender.setData(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, c.getLong(COLUMN_ID)));
                    
                    // 创建待处理意图
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, sender, 0);
                    
                    // 获取闹钟管理器并设置闹钟
                    AlarmManager alermManager = (AlarmManager) context
                            .getSystemService(Context.ALARM_SERVICE);
                    alermManager.set(AlarmManager.RTC_WAKEUP, alertDate, pendingIntent);
                } while (c.moveToNext());
            }
            // 关闭游标
            c.close();
        }
    }
}
