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
