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

package net.micode.notes.model;

import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.FileNotFoundException;
import android.text.style.ImageSpan;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.CallNote;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.DataConstants;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.data.Notes.TextNote;
import net.micode.notes.tool.ResourceParser.NoteBgResources;


/**
 * 工作笔记类
 * 封装笔记操作，提供设置提醒时间的接口
 */
public class WorkingNote {
    // 笔记模型
    private Note mNote;
    // 笔记ID
    private long mNoteId;
    // 笔记内容
    public String mContent;
    // 笔记模式（普通模式或 checklist 模式）
    private int mMode;

    // 提醒日期
    private long mAlertDate;

    // 修改日期
    private long mModifiedDate;

    // 背景颜色ID
    private int mBgColorId;

    // 小部件ID
    private int mWidgetId;

    // 小部件类型
    private int mWidgetType;

    // 文件夹ID
    private long mFolderId;

    // 上下文
    private Context mContext;

    // 日志标签
    private static final String TAG = "WorkingNote";

    // 是否已删除
    private boolean mIsDeleted;

    // 笔记设置变化监听器
    private NoteSettingChangedListener mNoteSettingStatusListener;

    /**
     * 数据投影数组
     * 用于从数据库中查询笔记数据
     */
    public static final String[] DATA_PROJECTION = new String[] {
            DataColumns.ID,            // 数据ID
            DataColumns.CONTENT,       // 内容
            DataColumns.MIME_TYPE,     // mime类型
            DataColumns.DATA1,         // 数据1（用于存储模式）
            DataColumns.DATA2,         // 数据2
            DataColumns.DATA3,         // 数据3
            DataColumns.DATA4,         // 数据4
    };

    /**
     * 笔记投影数组
     * 用于从数据库中查询笔记信息
     */
    public static final String[] NOTE_PROJECTION = new String[] {
            NoteColumns.PARENT_ID,     // 父文件夹ID
            NoteColumns.ALERTED_DATE,  // 提醒日期
            NoteColumns.BG_COLOR_ID,   // 背景颜色ID
            NoteColumns.WIDGET_ID,     // 小部件ID
            NoteColumns.WIDGET_TYPE,   // 小部件类型
            NoteColumns.MODIFIED_DATE  // 修改日期
    };

    // 数据列索引
    private static final int DATA_ID_COLUMN = 0;                // 数据ID列索引
    private static final int DATA_CONTENT_COLUMN = 1;           // 内容列索引
    private static final int DATA_MIME_TYPE_COLUMN = 2;         // mime类型列索引
    private static final int DATA_MODE_COLUMN = 3;              // 模式列索引

    // 笔记列索引
    private static final int NOTE_PARENT_ID_COLUMN = 0;         // 父文件夹ID列索引
    private static final int NOTE_ALERTED_DATE_COLUMN = 1;      // 提醒日期列索引
    private static final int NOTE_BG_COLOR_ID_COLUMN = 2;       // 背景颜色ID列索引
    private static final int NOTE_WIDGET_ID_COLUMN = 3;         // 小部件ID列索引
    private static final int NOTE_WIDGET_TYPE_COLUMN = 4;       // 小部件类型列索引
    private static final int NOTE_MODIFIED_DATE_COLUMN = 5;     // 修改日期列索引

    /**
     * 构造方法（新建笔记）
     * @param context 上下文
     * @param folderId 文件夹ID
     */
    private WorkingNote(Context context, long folderId) {
        mContext = context;
        mAlertDate = 0;
        mModifiedDate = System.currentTimeMillis();
        mFolderId = folderId;
        mNote = new Note();
        mNoteId = 0;
        mIsDeleted = false;
        mMode = 0;
        mWidgetType = Notes.TYPE_WIDGET_INVALIDE;
    }

    /**
     * 构造方法（现有笔记）
     * @param context 上下文
     * @param noteId 笔记ID
     * @param folderId 文件夹ID
     */
    private WorkingNote(Context context, long noteId, long folderId) {
        mContext = context;
        mNoteId = noteId;
        mFolderId = folderId;
        mIsDeleted = false;
        mNote = new Note();
        loadNote();
    }

    /**
     * 加载笔记数据
     * 从数据库中查询笔记的基本信息
     */
    private void loadNote() {
        Cursor cursor = mContext.getContentResolver().query(
                ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, mNoteId), NOTE_PROJECTION, null,
                null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mFolderId = cursor.getLong(NOTE_PARENT_ID_COLUMN);
                mBgColorId = cursor.getInt(NOTE_BG_COLOR_ID_COLUMN);
                mWidgetId = cursor.getInt(NOTE_WIDGET_ID_COLUMN);
                mWidgetType = cursor.getInt(NOTE_WIDGET_TYPE_COLUMN);
                mAlertDate = cursor.getLong(NOTE_ALERTED_DATE_COLUMN);
                mModifiedDate = cursor.getLong(NOTE_MODIFIED_DATE_COLUMN);
            }
            cursor.close();
        } else {
            Log.e(TAG, "No note with id:" + mNoteId);
            throw new IllegalArgumentException("Unable to find note with id " + mNoteId);
        }
        loadNoteData();
    }

    /**
     * 加载笔记详细数据
     * 从数据库中查询笔记的详细内容和类型
     */
    private void loadNoteData() {
        Cursor cursor = mContext.getContentResolver().query(Notes.CONTENT_DATA_URI, DATA_PROJECTION,
                DataColumns.NOTE_ID + "=?", new String[] {
                    String.valueOf(mNoteId)
                }, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(DATA_MIME_TYPE_COLUMN);
                    if (DataConstants.NOTE.equals(type)) {
                        mContent = cursor.getString(DATA_CONTENT_COLUMN);
                        mMode = cursor.getInt(DATA_MODE_COLUMN);
                        mNote.setTextDataId(cursor.getLong(DATA_ID_COLUMN));
                    } else if (DataConstants.CALL_NOTE.equals(type)) {
                        mNote.setCallDataId(cursor.getLong(DATA_ID_COLUMN));
                    } else {
                        Log.d(TAG, "Wrong note type with type:" + type);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            Log.e(TAG, "No data with id:" + mNoteId);
            throw new IllegalArgumentException("Unable to find note's data with id " + mNoteId);
        }
    }

    /**
     * 创建空笔记
     * @param context 上下文
     * @param folderId 文件夹ID
     * @param widgetId 小部件ID
     * @param widgetType 小部件类型
     * @param defaultBgColorId 默认背景颜色ID
     * @return 工作笔记实例
     */
    public static WorkingNote createEmptyNote(Context context, long folderId, int widgetId,
            int widgetType, int defaultBgColorId) {
        WorkingNote note = new WorkingNote(context, folderId);
        note.setBgColorId(defaultBgColorId);
        note.setWidgetId(widgetId);
        note.setWidgetType(widgetType);
        return note;
    }

    /**
     * 加载现有笔记
     * @param context 上下文
     * @param id 笔记ID
     * @return 工作笔记实例
     */
    public static WorkingNote load(Context context, long id) {
        return new WorkingNote(context, id, 0);
    }

    /**
     * 保存笔记
     * @return 是否保存成功
     */
    public synchronized boolean saveNote() {
        if (isWorthSaving()) {
            if (!existInDatabase()) {
                if ((mNoteId = Note.getNewNoteId(mContext, mFolderId)) == 0) {
                    Log.e(TAG, "Create new note fail with id:" + mNoteId);
                    return false;
                }
            }

            mNote.syncNote(mContext, mNoteId);

            /**
             * 如果笔记有对应的小部件，更新小部件内容
             */
            if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                    && mWidgetType != Notes.TYPE_WIDGET_INVALIDE
                    && mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onWidgetChanged();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查笔记是否存在于数据库中
     * @return 是否存在于数据库中
     */
    public boolean existInDatabase() {
        return mNoteId > 0;
    }

    /**
     * 检查笔记是否值得保存
     * @return 是否值得保存
     */
    private boolean isWorthSaving() {
        if (mIsDeleted || (!existInDatabase() && TextUtils.isEmpty(mContent))
                || (existInDatabase() && !mNote.isLocalModified())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 设置笔记设置变化监听器
     * @param l 监听器
     */
    public void setOnSettingStatusChangedListener(NoteSettingChangedListener l) {
        mNoteSettingStatusListener = l;
    }

    /**
     * 设置提醒日期
     * @param date 提醒日期（毫秒）
     * @param set 是否设置提醒
     */
    public void setAlertDate(long date, boolean set) {
        if (date != mAlertDate) {
            mAlertDate = date;
            mNote.setNoteValue(NoteColumns.ALERTED_DATE, String.valueOf(mAlertDate));
        }
        if (mNoteSettingStatusListener != null) {
            mNoteSettingStatusListener.onClockAlertChanged(date, set);
        }
    }

    /**
     * 标记笔记为已删除
     * @param mark 是否标记为已删除
     */
    public void markDeleted(boolean mark) {
        mIsDeleted = mark;
        if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID
                && mWidgetType != Notes.TYPE_WIDGET_INVALIDE && mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onWidgetChanged();
        }
    }

    /**
     * 设置背景颜色ID
     * @param id 背景颜色ID
     */
    public void setBgColorId(int id) {
        if (id != mBgColorId) {
            mBgColorId = id;
            if (mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onBackgroundColorChanged();
            }
            mNote.setNoteValue(NoteColumns.BG_COLOR_ID, String.valueOf(id));
        }
    }

    /**
     * 设置清单模式
     * @param mode 模式（0为普通模式，1为清单模式）
     */
    public void setCheckListMode(int mode) {
        if (mMode != mode) {
            if (mNoteSettingStatusListener != null) {
                mNoteSettingStatusListener.onCheckListModeChanged(mMode, mode);
            }
            mMode = mode;
            mNote.setTextData(TextNote.MODE, String.valueOf(mMode));
        }

    }

    /**
     * 设置小部件类型
     * @param type 小部件类型
     */
    public void setWidgetType(int type) {
        if (type != mWidgetType) {
            mWidgetType = type;
            mNote.setNoteValue(NoteColumns.WIDGET_TYPE, String.valueOf(type));
        }
    }

    /**
     * 设置小部件ID
     * @param id 小部件ID
     */
    public void setWidgetId(int id) {
        if (id != mWidgetId) {
            mWidgetId = id;
            mNote.setNoteValue(NoteColumns.WIDGET_ID, String.valueOf(id));
        }
    }

    /**
     * 设置笔记内容
     * @param text 笔记内容
     */
    public void setWorkingText(String text) {
        if (!TextUtils.equals(mContent, text)) {
            mContent = text;
            mNote.setTextData(TextNote.CONTENT, text);
        }
    }

    /**
     * 转换为通话笔记
     * @param phoneNumber 电话号码
     * @param callDate 通话日期（毫秒）
     */
    public void convertToCallNote(String phoneNumber, long callDate) {
        mNote.setCallData(CallNote.CALL_DATE, String.valueOf(callDate));
        mNote.setCallData(CallNote.PHONE_NUMBER, phoneNumber);
        mNote.setNoteValue(NoteColumns.PARENT_ID, String.valueOf(Notes.ID_CALL_RECORD_FOLDER));
    }

    /**
     * 检查是否有提醒
     * @return 是否有提醒
     */
    public boolean hasClockAlert() {
        return (mAlertDate > 0 ? true : false);
    }

    /**
     * 获取笔记内容
     * @return 笔记内容
     */
    public String getContent() {
        return mContent;
    }

    /**
     * 获取提醒日期
     * @return 提醒日期（毫秒）
     */
    public long getAlertDate() {
        return mAlertDate;
    }

    /**
     * 获取修改日期
     * @return 修改日期（毫秒）
     */
    public long getModifiedDate() {
        return mModifiedDate;
    }

    /**
     * 获取背景颜色资源ID
     * @return 背景颜色资源ID
     */
    public int getBgColorResId() {
        return NoteBgResources.getNoteBgResource(mBgColorId);
    }

    /**
     * 获取背景颜色ID
     * @return 背景颜色ID
     */
    public int getBgColorId() {
        return mBgColorId;
    }

    /**
     * 获取标题背景资源ID
     * @return 标题背景资源ID
     */
    public int getTitleBgResId() {
        return NoteBgResources.getNoteTitleBgResource(mBgColorId);
    }

    /**
     * 获取清单模式
     * @return 清单模式（0为普通模式，1为清单模式）
     */
    public int getCheckListMode() {
        return mMode;
    }

    /**
     * 获取笔记ID
     * @return 笔记ID
     */
    public long getNoteId() {
        return mNoteId;
    }

    /**
     * 获取文件夹ID
     * @return 文件夹ID
     */
    public long getFolderId() {
        return mFolderId;
    }

    /**
     * 获取小部件ID
     * @return 小部件ID
     */
    public int getWidgetId() {
        return mWidgetId;
    }

    /**
     * 获取小部件类型
     * @return 小部件类型
     */
    public int getWidgetType() {
        return mWidgetType;
    }

    /**
     * 笔记设置变化监听器
     */
    public interface NoteSettingChangedListener {
        /**
         * 当前笔记的背景颜色发生变化时调用
         */
        void onBackgroundColorChanged();

        /**
         * 用户设置提醒时调用
         * @param date 提醒日期（毫秒）
         * @param set 是否设置提醒
         */
        void onClockAlertChanged(long date, boolean set);

        /**
         * 用户从小部件创建笔记时调用
         */
        void onWidgetChanged();

        /**
         * 在清单模式和普通模式之间切换时调用
         * @param oldMode 切换前的模式
         * @param newMode 新的模式
         */
        void onCheckListModeChanged(int oldMode, int newMode);
    }
}
