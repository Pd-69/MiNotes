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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 闹钟广播接收器
 * 用于接收闹钟触发的广播，并启动闹钟提醒活动
 */
public class AlarmReceiver extends BroadcastReceiver {
    /**
     * 接收广播时调用
     * 1. 将意图的目标类设置为 AlarmAlertActivity
     * 2. 添加 FLAG_ACTIVITY_NEW_TASK 标志，确保在非活动上下文下也能启动活动
     * 3. 启动闹钟提醒活动
     * @param context 上下文
     * @param intent 接收到的广播意图
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // 将意图的目标类设置为 AlarmAlertActivity
        intent.setClass(context, AlarmAlertActivity.class);
        // 添加 FLAG_ACTIVITY_NEW_TASK 标志，确保在非活动上下文下也能启动活动
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 启动闹钟提醒活动
        context.startActivity(intent);
    }
}
