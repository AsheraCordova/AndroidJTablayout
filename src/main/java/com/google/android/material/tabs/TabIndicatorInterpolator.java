package com.google.android.material.tabs;
import static com.google.android.material.animation.MaterialAnimationUtils.lerp;
import r.android.graphics.RectF;
import r.android.graphics.drawable.Drawable;
import r.android.view.View;
import com.google.android.material.tabs.TabLayout.SlidingTabIndicator;
import com.google.android.material.tabs.TabLayout.TabView;
class TabIndicatorInterpolator {
  private static final int MIN_INDICATOR_WIDTH=24;
  static RectF calculateTabViewContentBounds(  TabView tabView,  int minWidth){
    int tabViewContentWidth=tabView.getContentWidth();
    int tabViewContentHeight=tabView.getContentHeight();
    int minWidthPx=(int)com.ashera.widget.PluginInvoker.convertDpToPixel(minWidth + "dp");
    if (tabViewContentWidth < minWidthPx) {
      tabViewContentWidth=minWidthPx;
    }
    int tabViewCenterX=(tabView.getLeft() + tabView.getRight()) / 2;
    int tabViewCenterY=(tabView.getTop() + tabView.getBottom()) / 2;
    int contentLeftBounds=tabViewCenterX - (tabViewContentWidth / 2);
    int contentTopBounds=tabViewCenterY - (tabViewContentHeight / 2);
    int contentRightBounds=tabViewCenterX + (tabViewContentWidth / 2);
    int contentBottomBounds=tabViewCenterY + (tabViewCenterX / 2);
    return new RectF(contentLeftBounds,contentTopBounds,contentRightBounds,contentBottomBounds);
  }
  static RectF calculateIndicatorWidthForTab(  TabLayout tabLayout,  View tab){
    if (tab == null) {
      return new RectF();
    }
    if (!tabLayout.isTabIndicatorFullWidth() && tab instanceof TabView) {
      return calculateTabViewContentBounds((TabView)tab,MIN_INDICATOR_WIDTH);
    }
    return new RectF(tab.getLeft(),tab.getTop(),tab.getRight(),tab.getBottom());
  }
  void setIndicatorBoundsForTab(  TabLayout tabLayout,  View tab,  Drawable indicator){
    RectF startIndicator=calculateIndicatorWidthForTab(tabLayout,tab);
    indicator.setBounds((int)startIndicator.left,indicator.getBounds().top,(int)startIndicator.right,indicator.getBounds().bottom);
  }
  void updateIndicatorForOffset(  TabLayout tabLayout,  View startTitle,  View endTitle,  float offset,  Drawable indicator){
    RectF startIndicator=calculateIndicatorWidthForTab(tabLayout,startTitle);
    RectF endIndicator=calculateIndicatorWidthForTab(tabLayout,endTitle);
    indicator.setBounds(lerp((int)startIndicator.left,(int)endIndicator.left,offset),indicator.getBounds().top,lerp((int)startIndicator.right,(int)endIndicator.right,offset),indicator.getBounds().bottom);
  }
}
