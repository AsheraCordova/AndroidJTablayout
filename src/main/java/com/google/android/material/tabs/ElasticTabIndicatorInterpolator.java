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
