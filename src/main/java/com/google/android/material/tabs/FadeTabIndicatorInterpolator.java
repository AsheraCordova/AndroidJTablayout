//start - license
/*
 * Copyright (c) 2025 Ashera Cordova
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
 */
//end - license
/*
 * Copyright (C) 2021 The Android Open Source Project
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
 * limitations under the License.
 */
package com.google.android.material.tabs;
import static com.google.android.material.animation.MaterialAnimationUtils.lerp;
import r.android.graphics.RectF;
import r.android.graphics.drawable.Drawable;
import r.android.view.View;
class FadeTabIndicatorInterpolator extends TabIndicatorInterpolator {
  private static final float FADE_THRESHOLD=0.5F;
  void updateIndicatorForOffset(  TabLayout tabLayout,  View startTitle,  View endTitle,  float offset,  Drawable indicator){
    View tab=offset < FADE_THRESHOLD ? startTitle : endTitle;
    RectF bounds=calculateIndicatorWidthForTab(tabLayout,tab);
    float alpha=offset < FADE_THRESHOLD ? lerp(1F,0F,0F,FADE_THRESHOLD,offset) : lerp(0F,1F,FADE_THRESHOLD,1F,offset);
    indicator.setBounds((int)bounds.left,indicator.getBounds().top,(int)bounds.right,indicator.getBounds().bottom);
    indicator.setAlpha((int)(alpha * 255F));
  }
}
