package online.sniper.widget.refresh;
/*
*  Copyright 2016 shizhefei（LuckyJayce）
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

import android.view.View;

import java.util.HashSet;

/**
 * Created by LuckyJayce on 2016/11/27.
 */
class ProxyPullHeader implements PullHeader {

    private HashSet<OnPullListener> mListeners = new HashSet<>(3);

    private PullHeader mPullHeader;

    public ProxyPullHeader(PullHeader pullHeader) {
        this.mPullHeader = pullHeader;
    }

    public void setPullHandler(PullHeader pullHeader) {
        mPullHeader = pullHeader;
    }

    @Override
    public View createHeaderView(CoolRefreshView refreshView) {
        return mPullHeader.createHeaderView(refreshView);
    }

    @Override
    public Config getConfig() {
        return mPullHeader.getConfig();
    }

    @Override
    public void onPullBegin(CoolRefreshView refreshView) {
        mPullHeader.onPullBegin(refreshView);
        for (OnPullListener listener : mListeners) {
            listener.onPullBegin(refreshView);
        }
    }

    @Override
    public void onPositionChange(CoolRefreshView refreshView, int status, int dy, int currentDistance) {
        mPullHeader.onPositionChange(refreshView, status, dy, currentDistance);
        for (OnPullListener listener : mListeners) {
            listener.onPositionChange(refreshView, status, dy, currentDistance);
        }
    }

    @Override
    public void onRefreshing(CoolRefreshView refreshView) {
        mPullHeader.onRefreshing(refreshView);
        for (OnPullListener listener : mListeners) {
            listener.onRefreshing(refreshView);
        }
    }

    @Override
    public void onReset(CoolRefreshView refreshView, boolean pullRelease) {
        mPullHeader.onReset(refreshView, pullRelease);
        for (OnPullListener listener : mListeners) {
            listener.onReset(refreshView, pullRelease);
        }
    }

    @Override
    public void onPullRefreshComplete(CoolRefreshView refreshView) {
        mPullHeader.onPullRefreshComplete(refreshView);
        for (OnPullListener listener : mListeners) {
            listener.onPullRefreshComplete(refreshView);
        }
    }

    public void addListener(OnPullListener onPullListener) {
        mListeners.add(onPullListener);
    }

    public void removeListener(OnPullListener onPullListener) {
        mListeners.remove(onPullListener);
    }
}
