/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.pascal.frontend.editor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.pager.PageDescriptor;
import com.commonsware.cwac.pager.SimplePageDescriptor;
import com.duy.pascal.frontend.DLog;
import com.duy.pascal.frontend.EditorControl;
import com.duy.pascal.frontend.R;
import com.duy.pascal.frontend.activities.BaseActivity;
import com.duy.pascal.frontend.code.CompileManager;
import com.duy.pascal.frontend.file.FileActionCallback;
import com.duy.pascal.frontend.file.FileExplorerView;
import com.duy.pascal.frontend.file.FileManager;
import com.duy.pascal.frontend.file.TabFileUtils;
import com.duy.pascal.frontend.file.fragment.FileListPagerFragment;
import com.duy.pascal.frontend.file.io.LocalFile;
import com.duy.pascal.frontend.setting.PascalPreferences;
import com.duy.pascal.frontend.view.SymbolListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Duy on 09-Mar-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public abstract class BaseEditorActivity extends BaseActivity //for debug
        implements SymbolListView.OnKeyListener,
        EditorControl,
        FileActionCallback,
        EditorContext {
    protected final static String TAG = BaseEditorActivity.class.getSimpleName();
    protected final boolean SELECT = true;
    protected final boolean SAVE_LAST_FILE = true;
    protected final boolean UN_SELECT = false;
    protected final boolean UN_SAVE_LAST_FILE = false;
    protected FileManager mFileManager;
    protected EditorPagerAdapter mPagerAdapter;
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    DrawerLayout mDrawerLayout;
    SymbolListView mKeyList;
    NavigationView mNavigationView;
    TabLayout mTabLayout;
    View mContainerSymbol;
    ViewPager mViewPager;
    private KeyBoardEventListener keyBoardListener;
    private FileListPagerFragment mFileExplorer;

    protected void onShowKeyboard() {
        hideAppBar();
    }

    protected void onHideKeyboard() {
        showAppBar();
    }

    /**
     * hide appbar layout when keyboard visible
     */
    private void hideAppBar() {
        mTabLayout.setVisibility(View.GONE);
    }

    /**
     * show appbar layout when keyboard gone
     */
    private void showAppBar() {
        mTabLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DLog.d(TAG, "onCreate: ");

        setContentView(R.layout.activity_editor);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mKeyList = findViewById(R.id.recycler_view);
        mFileManager = new FileManager(this);
        mNavigationView = findViewById(R.id.navigation_view);
        mTabLayout = findViewById(R.id.tab_layout);
        mContainerSymbol = findViewById(R.id.container_symbol);
        mViewPager = findViewById(R.id.view_pager);

        setupToolbar();
        setupPageView();
        initFileView();
    }

    private void initFileView() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFileExplorer = (FileListPagerFragment) fragmentManager.findFragmentByTag(FileListPagerFragment.TAG);
        if (mFileExplorer == null) {
            mFileExplorer = FileListPagerFragment.newFragment(new LocalFile(FileManager.getFilePath()))
        }
        FragmentTransaction fm = fragmentManager.beginTransaction();
        fm.replace(R.layout.file_explorer, mFileExplorer, FileListPagerFragment.TAG).commit();
    }

    protected void setupPageView() {
        ArrayList<File> listFile = TabFileUtils.getTabFiles(this);
        ArrayList<PageDescriptor> pages = new ArrayList<>();
        for (File file : listFile) {
            pages.add(new SimplePageDescriptor(file.getPath(), file.getName()));
        }
        mPagerAdapter = new EditorPagerAdapter(getSupportFragmentManager(), pages);
        mViewPager.setAdapter(mPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        invalidateTab();

        if (mPagerAdapter.getCount() == 0) {
            String fileName = Integer.toHexString((int) System.currentTimeMillis()) + ".pas";
            String filePath = mFileManager.createNewFileInMode(fileName);
            addNewPageEditor(new File(filePath), SELECT);
        }

        int pos = getPreferences().getInt(PascalPreferences.TAB_POSITION_FILE);
        if (mPagerAdapter.getCount() > pos) {
            mViewPager.setCurrentItem(pos);
        }
    }

    private void invalidateTab() {
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            final TabLayout.Tab tab = mTabLayout.getTabAt(i);
            View view = null;
            if (tab != null) {
                tab.setCustomView(R.layout.item_tab_file);
                view = tab.getCustomView();
            }

            if (view != null) {
                View vClose = view.findViewById(R.id.img_close);
                final int position = i;
                vClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removePage(position);
                    }
                });
                TextView txtTitle = view.findViewById(R.id.txt_name);
                txtTitle.setText(mPagerAdapter.getPageTitle(i));
                txtTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(position);
                    }
                });
            }

            if (i == mViewPager.getCurrentItem()) {
                if (tab != null) {
                    tab.select();
                }
            }
        }
    }

    protected void setupToolbar() {
        //setup action bar
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        //attach listener hide/show keyboard
        keyBoardListener = new KeyBoardEventListener(this);
        mDrawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(keyBoardListener);
    }

    /**
     * remove a page in <code>position</code>
     */
    protected void removePage(int position) {
        Fragment existingFragment = mPagerAdapter.getExistingFragment(position);
        if (existingFragment == null) {
            if (DLog.DEBUG) DLog.d(TAG, "removePage: " + "null page " + position);
            return;
        }
        //delete in database
        String filePath = existingFragment.getTag();
        mFileManager.removeTabFile(filePath);

        //remove page
        mPagerAdapter.remove(position);
        invalidateTab();
        String msg = getString(R.string.closed) + " " + new File(filePath).getName();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        invalidateTab();
    }

    /**
     * Add new page for editor
     * Check if not in list file, add it to tab and select tab of file
     *
     * @param file          - file need load
     * @param selectNewPage - if <code>true</code>, the tab of file will be selected when initialized
     */
    protected void addNewPageEditor(@NonNull File file, boolean selectNewPage) {
        int position = mPagerAdapter.getPositionForTag(file.getPath());
        if (position != -1) { //existed in list file
            //check need select tab
            if (selectNewPage) {
                TabLayout.Tab tab = mTabLayout.getTabAt(position);
                if (tab != null) {
                    tab.select();
                    mViewPager.setCurrentItem(position);
                }
            }
        } else { //new file
            if (mPagerAdapter.getCount() >= getPreferences().getMaxPage()) {
                Fragment existingFragment = mPagerAdapter.getExistingFragment(0);
                if (existingFragment != null) {
                    mFileManager.removeTabFile(existingFragment.getTag());
                    removePage(0);
                }
            }

            //add to database
            mFileManager.addNewPath(file.getPath());

            //new page
            mPagerAdapter.add(new SimplePageDescriptor(file.getPath(), file.getName()));
            invalidateTab();

            if (selectNewPage) {
                int indexOfNewPage = mPagerAdapter.getCount() - 1;
                TabLayout.Tab tab = mTabLayout.getTabAt(indexOfNewPage);
                if (tab != null) {
                    tab.select();
                    mViewPager.setCurrentItem(indexOfNewPage);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferences().put(PascalPreferences.TAB_POSITION_FILE, mTabLayout.getSelectedTabPosition());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra(CompileManager.FILE_PATH) != null) {
                String filePath = intent.getStringExtra(CompileManager.FILE_PATH);
                //No need save last file because it is the frist file
                addNewPageEditor(new File(filePath), SELECT);
                //Remove path
                intent.removeExtra(CompileManager.FILE_PATH);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getStringExtra(CompileManager.FILE_PATH) != null) {
            String filePath = intent.getStringExtra(CompileManager.FILE_PATH);
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                return;
            }
            addNewPageEditor(file, SELECT);
            //remove path
            intent.removeExtra(CompileManager.FILE_PATH);
        }
    }

    protected abstract String getCode();

    /**
     * delete a file
     *
     * @param file - file need delete
     * @return true if delete success
     */
    @Override
    public boolean doRemoveFile(final File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.remove_file_msg) + " " + file.getName());
        builder.setTitle(R.string.delete_file);
        builder.setIcon(R.drawable.ic_delete_forever_white_24dp);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = mPagerAdapter.getPositionForTag(file.getPath());
                boolean success = mFileManager.deleteFile(file);
                if (success) {
                    if (position >= 0) {
                        removePage(position);
                    }
                    Toast.makeText(getApplicationContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), R.string.failed, Toast.LENGTH_SHORT).show();

                //reload file
                FileExplorerView controller = (FileExplorerView) getSupportFragmentManager().findFragmentByTag("fragment_file_view");
                if (controller != null) {
                    controller.refresh();
                } else {
                    DLog.d(TAG, "onClick: Fragment file is null");
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
        return false;
    }

    /**
     * @return current file selected
     */
    @Nullable
    @Override
    public File getCurrentFile() {
        EditorFragment editorFragment = mPagerAdapter.getCurrentFragment();
        if (editorFragment != null) {
            String filePath = editorFragment.getFilePath();
            return new File(filePath);
        }
        return null;
    }

    @Override
    public void saveAs() {
        saveFile();
        final AppCompatEditText edittext = new AppCompatEditText(this);
        edittext.setHint(R.string.enter_new_file_name);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.save_as)
                .setView(edittext)
                .setIcon(R.drawable.ic_create_new_folder_white_24dp)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String fileName = edittext.getText().toString();
                        dialog.cancel();
                        File currentFile = getCurrentFile();
                        if (currentFile != null) {
                            try {
                                mFileManager.copy(currentFile.getPath(),
                                        currentFile.getParent() + "/" + fileName);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(BaseEditorActivity.this, R.string.can_not_save_file,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeKeyBoard();
        mDrawerLayout.getViewTreeObserver()
                .removeGlobalOnLayoutListener(keyBoardListener);

    }

    // closes the soft keyboard
    protected void closeKeyBoard() throws NullPointerException {
        // Central system API to the overall input method framework (IMF) architecture
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            // Base interface for a remotable object
            IBinder windowToken = currentFocus.getWindowToken();

            // Hide type
            int hideType = InputMethodManager.HIDE_NOT_ALWAYS;

            // Hide the KeyBoard
            inputManager.hideSoftInputFromWindow(windowToken, hideType);
        }
    }


    private class KeyBoardEventListener implements ViewTreeObserver.OnGlobalLayoutListener {
        BaseEditorActivity activity;

        KeyBoardEventListener(BaseEditorActivity activityIde) {
            this.activity = activityIde;
        }

        public void onGlobalLayout() {
            int i = 0;
            int navHeight = this.activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            navHeight = navHeight > 0 ? this.activity.getResources().getDimensionPixelSize(navHeight) : 0;
            int statusBarHeight = this.activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarHeight > 0) {
                i = this.activity.getResources().getDimensionPixelSize(statusBarHeight);
            }
            Rect rect = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            if (activity.mDrawerLayout.getRootView().getHeight() - ((navHeight + i) + rect.height()) <= 0) {
                activity.onHideKeyboard();
            } else {
                activity.onShowKeyboard();
            }
        }
    }


}
