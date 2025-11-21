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
 * Copyright (C) 2020 The Android Open Source Project
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
class ElasticTabIndicatorInterpolator extends TabIndicatorInterpolator {
  private static float decInterp(  float fraction){
    return (float)Math.sin((fraction * Math.PI) / 2.0);
  }
  private static float accInterp(  float fraction){
    return (float)(1.0 - Math.cos((fraction * Math.PI) / 2.0));
  }
  void updateIndicatorForOffset(  TabLayout tabLayout,  View startTitle,  View endTitle,  float offset,  Drawable indicator){
    RectF startIndicator=calculateIndicatorWidthForTab(tabLayout,startTitle);
    RectF endIndicator=calculateIndicatorWidthForTab(tabLayout,endTitle);
    float leftFraction;
    float rightFraction;
    final boolean isMovingRight=startIndicator.left < endIndicator.left;
    if (isMovingRight) {
      leftFraction=accInterp(offset);
      rightFraction=decInterp(offset);
    }
 else {
      leftFraction=decInterp(offset);
      rightFraction=accInterp(offset);
    }
    indicator.setBounds(lerp((int)startIndicator.left,(int)endIndicator.left,leftFraction),indicator.getBounds().top,lerp((int)startIndicator.right,(int)endIndicator.right,rightFraction),indicator.getBounds().bottom);
  }
}
