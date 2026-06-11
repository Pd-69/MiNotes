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

import java.util.Calendar;

import net.micode.notes.R;
import net.micode.notes.ui.DateTimePicker;
import net.micode.notes.ui.DateTimePicker.OnDateTimeChangedListener;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

/**
 * 日期时间选择对话框
 * 用于显示日期时间选择器并处理用户选择
 */
public class DateTimePickerDialog extends AlertDialog implements OnClickListener {

    // 日期对象，用于存储选择的日期时间
    private Calendar mDate = Calendar.getInstance();
    // 是否使用24小时制
    private boolean mIs24HourView;
    // 日期时间设置监听器
    private OnDateTimeSetListener mOnDateTimeSetListener;
    // 日期时间选择器
    private DateTimePicker mDateTimePicker;

    /**
     * 日期时间设置监听器接口
     * 当用户设置日期时间并点击确定按钮时回调
     */
    public interface OnDateTimeSetListener {
        /**
         * 日期时间设置完成时调用
         * @param dialog 对话框实例
         * @param date 选择的日期时间（毫秒）
         */
        void OnDateTimeSet(AlertDialog dialog, long date);
    }

    /**
     * 构造方法
     * @param context 上下文
     * @param date 初始日期时间（毫秒）
     */
    public DateTimePickerDialog(Context context, long date) {
        super(context);
        // 初始化日期时间选择器
        mDateTimePicker = new DateTimePicker(context);
        setView(mDateTimePicker);
        
        // 设置日期时间变化监听器
        mDateTimePicker.setOnDateTimeChangedListener(new OnDateTimeChangedListener() {
            public void onDateTimeChanged(DateTimePicker view, int year, int month,
                    int dayOfMonth, int hourOfDay, int minute) {
                // 更新日期对象
                mDate.set(Calendar.YEAR, year);
                mDate.set(Calendar.MONTH, month);
                mDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                mDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mDate.set(Calendar.MINUTE, minute);
                // 更新对话框标题
                updateTitle(mDate.getTimeInMillis());
            }
        });
        
        // 设置初始日期时间
        mDate.setTimeInMillis(date);
        mDate.set(Calendar.SECOND, 0);
        mDateTimePicker.setCurrentDate(mDate.getTimeInMillis());
        
        // 设置对话框按钮
        setButton(context.getString(R.string.datetime_dialog_ok), this);
        setButton2(context.getString(R.string.datetime_dialog_cancel), (OnClickListener)null);
        
        // 设置时间制
        set24HourView(DateFormat.is24HourFormat(this.getContext()));
        
        // 更新对话框标题
        updateTitle(mDate.getTimeInMillis());
    }

    /**
     * 设置是否使用24小时制
     * @param is24HourView 是否使用24小时制
     */
    public void set24HourView(boolean is24HourView) {
        mIs24HourView = is24HourView;
    }

    /**
     * 设置日期时间设置监听器
     * @param callBack 监听器回调
     */
    public void setOnDateTimeSetListener(OnDateTimeSetListener callBack) {
        mOnDateTimeSetListener = callBack;
    }

    /**
     * 更新对话框标题
     * 根据日期时间和时间制格式更新对话框标题
     * @param date 日期时间（毫秒）
     */
    private void updateTitle(long date) {
        int flag = 
            DateUtils.FORMAT_SHOW_YEAR |
            DateUtils.FORMAT_SHOW_DATE |
            DateUtils.FORMAT_SHOW_TIME;
        flag |= mIs24HourView ? DateUtils.FORMAT_24HOUR : DateUtils.FORMAT_24HOUR;
        setTitle(DateUtils.formatDateTime(this.getContext(), date, flag));
    }

    /**
     * 对话框按钮点击事件处理
     * 当用户点击确定按钮时调用
     * @param arg0 对话框实例
     * @param arg1 按钮类型
     */
    public void onClick(DialogInterface arg0, int arg1) {
        if (mOnDateTimeSetListener != null) {
            mOnDateTimeSetListener.OnDateTimeSet(this, mDate.getTimeInMillis());
        }
    }

}