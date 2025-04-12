/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package android.app;

import android.content.Context;
import android.os.RemoteException;

public class ESIMManager {
    Context mContext;
    IESIMAidlInterface mEsimAidlInterface;
    public ESIMManager(Context context,IESIMAidlInterface iesimAidlInterface) {
        this.mContext = context;
        this.mEsimAidlInterface = iesimAidlInterface;
    }

    public void addCard(String str1,String str2,String str3,String str4,String str5){
        if (mEsimAidlInterface!=null){
            try{
                mEsimAidlInterface.addCard(str1, str2, str3, str4, str5);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
