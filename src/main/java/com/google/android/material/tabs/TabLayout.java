package com.google.android.material.tabs;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE;
import static androidx.viewpager.widget.ViewPager.SCROLL_STATE_SETTLING;
import r.android.animation.TimeInterpolator;
import r.android.animation.ValueAnimator;
import r.android.content.Context;
import r.android.content.res.ColorStateList;
import r.android.database.DataSetObserver;
import r.android.graphics.Canvas;
import r.android.graphics.Color;
import r.android.graphics.Rect;
import r.android.graphics.drawable.Drawable;
import r.android.os.Build;
import r.android.os.Build.VERSION;
import r.android.os.Build.VERSION_CODES;
import r.android.text.TextUtils;
import r.android.util.Log;
import r.android.view.Gravity;
import r.android.view.LayoutInflater;
import r.android.view.View;
import r.android.view.ViewGroup;
import r.android.view.ViewParent;
import r.android.widget.FrameLayout;
import r.android.widget.HorizontalScrollView;
import r.android.widget.ImageView;
import r.android.widget.LinearLayout;
import r.android.widget.TextView;
import androidx.core.view.GravityCompat;
import androidx.core.view.MarginLayoutParamsCompat;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
public class TabLayout extends HorizontalScrollView {
  private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON=72;
  static final int DEFAULT_GAP_TEXT_ICON=8;
  private static final int DEFAULT_HEIGHT=48;
  private static final int TAB_MIN_WIDTH_MARGIN=56;
  static final int FIXED_WRAP_GUTTER_MIN=16;
  private static final int INVALID_WIDTH=-1;
  private static final int ANIMATION_DURATION=300;
  private static final int SELECTED_INDICATOR_HEIGHT_DEFAULT=-1;
  private static final r.android.util.Pools.Pool<Tab> tabPool=new r.android.util.Pools.SynchronizedPool<>(16);
  private static final String LOG_TAG="TabLayout";
  public static final int MODE_SCROLLABLE=0;
  public static final int MODE_FIXED=1;
  public static final int MODE_AUTO=2;
  public static final int TAB_LABEL_VISIBILITY_UNLABELED=0;
  public static final int TAB_LABEL_VISIBILITY_LABELED=1;
  public static final int GRAVITY_FILL=0;
  public static final int GRAVITY_CENTER=1;
  public static final int GRAVITY_START=1 << 1;
  int indicatorPosition=-1;
  public static final int INDICATOR_GRAVITY_BOTTOM=0;
  public static final int INDICATOR_GRAVITY_CENTER=1;
  public static final int INDICATOR_GRAVITY_TOP=2;
  public static final int INDICATOR_GRAVITY_STRETCH=3;
  public static final int INDICATOR_ANIMATION_MODE_LINEAR=0;
  public static final int INDICATOR_ANIMATION_MODE_ELASTIC=1;
  public static final int INDICATOR_ANIMATION_MODE_FADE=2;
public interface OnTabSelectedListener extends BaseOnTabSelectedListener<Tab> {
  }
public interface BaseOnTabSelectedListener<T extends Tab> {
    public void onTabSelected(    T tab);
    public void onTabUnselected(    T tab);
    public void onTabReselected(    T tab);
  }
  private final ArrayList<Tab> tabs=new ArrayList<>();
  private Tab selectedTab;
  SlidingTabIndicator slidingTabIndicator;
  int tabPaddingStart;
  int tabPaddingTop;
  int tabPaddingEnd;
  int tabPaddingBottom;
  private final String defaultTabTextAppearance;
  private String tabTextAppearance;
  private String selectedTabTextAppearance;  
  ColorStateList tabTextColors;
  ColorStateList tabIconTint;
  Drawable tabSelectedIndicator;
  private int tabSelectedIndicatorColor=Color.TRANSPARENT;
  float tabTextSize;
  float tabTextMultiLineSize;
  int tabBackgroundResId;
  int tabMaxWidth=Integer.MAX_VALUE;
  private int requestedTabMinWidth;
  private int requestedTabMaxWidth;
  private final int scrollableTabMinWidth;
  private int contentInsetStart;
  int tabGravity;
  int tabIndicatorAnimationDuration;
  int tabIndicatorGravity;
  int mode;
  boolean inlineLabel;
  boolean tabIndicatorFullWidth;
  int tabIndicatorHeight=SELECTED_INDICATOR_HEIGHT_DEFAULT;
  int tabIndicatorAnimationMode;
  private TabIndicatorInterpolator tabIndicatorInterpolator;
  private final TimeInterpolator tabIndicatorTimeInterpolator;
  private BaseOnTabSelectedListener selectedListener;
  private final ArrayList<BaseOnTabSelectedListener> selectedListeners=new ArrayList<>();
  private BaseOnTabSelectedListener currentVpSelectedListener;
  private ValueAnimator scrollAnimator;
  ViewPager viewPager;
  private PagerAdapter pagerAdapter;
  private DataSetObserver pagerAdapterObserver;
  private TabLayoutOnPageChangeListener pageChangeListener;
  private AdapterChangeListener adapterChangeListener;
  private boolean setupViewPagerImplicitly;
  private int viewPagerScrollState;
  private final r.android.util.Pools.Pool<TabView> tabViewPool=new r.android.util.Pools.SimplePool<>(12);
  public void setSelectedTabIndicatorColor(  int color){
    this.tabSelectedIndicatorColor=color;
    tabSelectedIndicator.setDrawable(tabSelectedIndicatorColor);
    updateTabViews(false);
  }
  public void setSelectedTabIndicatorHeight(  int height){
    tabIndicatorHeight=height;
    slidingTabIndicator.setSelectedIndicatorHeight(height);
  }
  public void setScrollPosition(  int position,  float positionOffset,  boolean updateSelectedTabView){
    setScrollPosition(position,positionOffset,updateSelectedTabView,true);
  }
  public void setScrollPosition(  int position,  float positionOffset,  boolean updateSelectedTabView,  boolean updateIndicatorPosition){
    setScrollPosition(position,positionOffset,updateSelectedTabView,updateIndicatorPosition,true);
  }
  public void setScrollPosition(  int position,  float positionOffset,  boolean updateSelectedTabView,  boolean updateIndicatorPosition,  boolean alwaysScroll) {
    final int roundedPosition=Math.round(position + positionOffset);
    if (roundedPosition < 0 || roundedPosition >= slidingTabIndicator.getChildCount()) {
      return;
    }
    if (updateIndicatorPosition) {
      slidingTabIndicator.setIndicatorPositionFromTabPosition(position,positionOffset);
    }
    if (scrollAnimator != null && scrollAnimator.isRunning()) {
      scrollAnimator.cancel();
    }
    int scrollXForPosition=calculateScrollXForTab(position,positionOffset);
    int scrollX=getScrollX();
    boolean toMove=(position < getSelectedTabPosition() && scrollXForPosition >= scrollX) || (position > getSelectedTabPosition() && scrollXForPosition <= scrollX) || (position == getSelectedTabPosition());
    if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
      toMove=(position < getSelectedTabPosition() && scrollXForPosition <= scrollX) || (position > getSelectedTabPosition() && scrollXForPosition >= scrollX) || (position == getSelectedTabPosition());
    }
    if (toMove || viewPagerScrollState == SCROLL_STATE_DRAGGING || alwaysScroll) {
      scrollTo(position < 0 ? 0 : scrollXForPosition,0);
    }
    if (updateSelectedTabView) {
      setSelectedTabView(roundedPosition);
    }
  }
  public void addTab(  Tab tab){
    addTab(tab,tabs.isEmpty());
  }
  public void addTab(  Tab tab,  int position){
    addTab(tab,position,tabs.isEmpty());
  }
  public void addTab(  Tab tab,  boolean setSelected){
    addTab(tab,tabs.size(),setSelected);
  }
  public void addTab(  Tab tab,  int position,  boolean setSelected){
    if (tab.parent != this) {
      throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
    }
    configureTab(tab,position);
    addTabView(tab);
    if (setSelected) {
      tab.select();
    }
  }
  public void setOnTabSelectedListener(  OnTabSelectedListener listener){
    setOnTabSelectedListener((BaseOnTabSelectedListener)listener);
  }
  public void setOnTabSelectedListener(  BaseOnTabSelectedListener listener){
    if (selectedListener != null) {
      removeOnTabSelectedListener(selectedListener);
    }
    selectedListener=listener;
    if (listener != null) {
      addOnTabSelectedListener(listener);
    }
  }
  public void addOnTabSelectedListener(  OnTabSelectedListener listener){
    addOnTabSelectedListener((BaseOnTabSelectedListener)listener);
  }
  public void addOnTabSelectedListener(  BaseOnTabSelectedListener listener){
    if (!selectedListeners.contains(listener)) {
      selectedListeners.add(listener);
    }
  }
  public void removeOnTabSelectedListener(  OnTabSelectedListener listener){
    removeOnTabSelectedListener((BaseOnTabSelectedListener)listener);
  }
  public void removeOnTabSelectedListener(  BaseOnTabSelectedListener listener){
    selectedListeners.remove(listener);
  }
  public Tab newTab(){
    Tab tab=createTabFromPool();
    tab.parent=this;
    tab.view=createTabView(tab);
    if (tab.id != NO_ID) {
      tab.view.setId(tab.id);
    }
    return tab;
  }
  protected Tab createTabFromPool(){
    Tab tab=tabPool.acquire();
    if (tab == null) {
      tab=new Tab();
    }
    return tab;
  }
  protected boolean releaseFromTabPool(  Tab tab){
    return tabPool.release(tab);
  }
  public int getTabCount(){
    return tabs.size();
  }
  public Tab getTabAt(  int index){
    return (index < 0 || index >= getTabCount()) ? null : tabs.get(index);
  }
  public int getSelectedTabPosition(){
    return selectedTab != null ? selectedTab.getPosition() : -1;
  }
  public void removeTab(  Tab tab){
    if (tab.parent != this) {
      throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
    }
    removeTabAt(tab.getPosition());
  }
  public void removeTabAt(  int position){
    final int selectedTabPosition=selectedTab != null ? selectedTab.getPosition() : 0;
    removeTabViewAt(position);
    final Tab removedTab=tabs.remove(position);
    if (removedTab != null) {
      removedTab.reset();
      releaseFromTabPool(removedTab);
    }
    final int newTabCount=tabs.size();
    int newIndicatorPosition=-1;
    for (int i=position; i < newTabCount; i++) {
      if (tabs.get(i).getPosition() == indicatorPosition) {
        newIndicatorPosition=i;
      }
      tabs.get(i).setPosition(i);
    }
    indicatorPosition=newIndicatorPosition;
    if (selectedTabPosition == position) {
      selectTab(tabs.isEmpty() ? null : tabs.get(Math.max(0,position - 1)));
    }
  }
  public void removeAllTabs(){
    for (int i=slidingTabIndicator.getChildCount() - 1; i >= 0; i--) {
      removeTabViewAt(i);
    }
    for (final Iterator<Tab> i=tabs.iterator(); i.hasNext(); ) {
      final Tab tab=i.next();
      i.remove();
      tab.reset();
      releaseFromTabPool(tab);
    }
    selectedTab=null;
  }
  public void setTabMode(  int mode){
    if (mode != this.mode) {
      this.mode=mode;
      applyModeAndGravity();
    }
  }
  public void setTabGravity(  int gravity){
    if (tabGravity != gravity) {
      tabGravity=gravity;
      applyModeAndGravity();
    }
  }
  public void setSelectedTabIndicatorGravity(  int indicatorGravity){
    if (tabIndicatorGravity != indicatorGravity) {
      tabIndicatorGravity=indicatorGravity;
      //ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
    }
  }
  public void setTabIndicatorAnimationMode(  int tabIndicatorAnimationMode){
    this.tabIndicatorAnimationMode=tabIndicatorAnimationMode;
switch (tabIndicatorAnimationMode) {
case INDICATOR_ANIMATION_MODE_LINEAR:
      this.tabIndicatorInterpolator=new TabIndicatorInterpolator();
    break;
case INDICATOR_ANIMATION_MODE_ELASTIC:
  this.tabIndicatorInterpolator=new ElasticTabIndicatorInterpolator();
break;
case INDICATOR_ANIMATION_MODE_FADE:
this.tabIndicatorInterpolator=new FadeTabIndicatorInterpolator();
break;
default :
throw new IllegalArgumentException(tabIndicatorAnimationMode + " is not a valid TabIndicatorAnimationMode");
}
}
public int getTabIndicatorAnimationMode(){
return tabIndicatorAnimationMode;
}
public void setTabIndicatorFullWidth(boolean tabIndicatorFullWidth){
this.tabIndicatorFullWidth=tabIndicatorFullWidth;
slidingTabIndicator.jumpIndicatorToSelectedPosition();
//ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
}
public boolean isTabIndicatorFullWidth(){
return tabIndicatorFullWidth;
}
public void setInlineLabel(boolean inline){
if (inlineLabel != inline) {
inlineLabel=inline;
for (int i=0; i < slidingTabIndicator.getChildCount(); i++) {
View child=slidingTabIndicator.getChildAt(i);
if (child instanceof TabView) {
((TabView)child).updateOrientation();
}
}
applyModeAndGravity();
}
}
public void setTabTextColors(ColorStateList textColor){
if (tabTextColors != textColor) {
tabTextColors=textColor;
updateAllTabs();
}
}
public ColorStateList getTabTextColors(){
return tabTextColors;
}
public void setTabTextColors(int normalColor,int selectedColor){
setTabTextColors(createColorStateList(normalColor,selectedColor));
}
public void setTabIconTint(ColorStateList iconTint){
if (tabIconTint != iconTint) {
tabIconTint=iconTint;
updateAllTabs();
}
}
public Drawable getTabSelectedIndicator(){
return tabSelectedIndicator;
}
public void setupWithViewPager(ViewPager viewPager){
setupWithViewPager(viewPager,true);
}
public void setupWithViewPager(final ViewPager viewPager,boolean autoRefresh){
setupWithViewPager(viewPager,autoRefresh,false);
}
private void setupWithViewPager(final ViewPager viewPager,boolean autoRefresh,boolean implicitSetup){
if (this.viewPager != null) {
if (pageChangeListener != null) {
this.viewPager.removeOnPageChangeListener(pageChangeListener);
}
if (adapterChangeListener != null) {
this.viewPager.removeOnAdapterChangeListener(adapterChangeListener);
}
}
if (currentVpSelectedListener != null) {
removeOnTabSelectedListener(currentVpSelectedListener);
currentVpSelectedListener=null;
}
if (viewPager != null) {
this.viewPager=viewPager;
if (pageChangeListener == null) {
pageChangeListener=new TabLayoutOnPageChangeListener(this);
}
pageChangeListener.reset();
viewPager.addOnPageChangeListener(pageChangeListener);
currentVpSelectedListener=new ViewPagerOnTabSelectedListener(viewPager);
addOnTabSelectedListener(currentVpSelectedListener);
final PagerAdapter adapter=viewPager.getAdapter();
if (adapter != null) {
setPagerAdapter(adapter,autoRefresh);
}
if (adapterChangeListener == null) {
adapterChangeListener=new AdapterChangeListener();
}
adapterChangeListener.setAutoRefresh(autoRefresh);
viewPager.addOnAdapterChangeListener(adapterChangeListener);
setScrollPosition(viewPager.getCurrentItem(),0f,true);
}
 else {
this.viewPager=null;
setPagerAdapter(null,false);
}
setupViewPagerImplicitly=implicitSetup;
}
void updateViewPagerScrollState(int scrollState){
this.viewPagerScrollState=scrollState;
}
void setPagerAdapter(final PagerAdapter adapter,final boolean addObserver){
if (pagerAdapter != null && pagerAdapterObserver != null) {
pagerAdapter.unregisterDataSetObserver(pagerAdapterObserver);
}
pagerAdapter=adapter;
if (addObserver && adapter != null) {
if (pagerAdapterObserver == null) {
pagerAdapterObserver=new PagerAdapterObserver();
}
adapter.registerDataSetObserver(pagerAdapterObserver);
}
populateFromPagerAdapter();
}
void populateFromPagerAdapter(){
removeAllTabs();
if (pagerAdapter != null) {
final int adapterCount=pagerAdapter.getCount();
for (int i=0; i < adapterCount; i++) {
addTab(newTab().setText(pagerAdapter.getPageTitle(i)),false);
}
if (viewPager != null && adapterCount > 0) {
final int curItem=viewPager.getCurrentItem();
if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
selectTab(getTabAt(curItem));
}
}
}
}
public void updateAllTabs(){
for (int i=0, z=tabs.size(); i < z; i++) {
tabs.get(i).updateView();
}
}
private TabView createTabView(final Tab tab){
TabView tabView=tabViewPool != null ? tabViewPool.acquire() : null;
if (tabView == null) {
tabView=(TabView)LayoutInflater.from(mContext).inflate("@layout/design_tab_view", slidingTabIndicator, true);;
}
tabView.setTab(tab);
tabView.setFocusable(true);
tabView.setMinimumWidth(getTabMinWidth());
if (TextUtils.isEmpty(tab.contentDesc)) {
//tabView.//setContentDescription(tab.text);
}
 else {
//tabView.//setContentDescription(tab.contentDesc);
}
return tabView;
}
private void configureTab(Tab tab,int position){
tab.setPosition(position);
tabs.add(position,tab);
final int count=tabs.size();
int newIndicatorPosition=-1;
for (int i=position + 1; i < count; i++) {
if (tabs.get(i).getPosition() == indicatorPosition) {
newIndicatorPosition=i;
}
tabs.get(i).setPosition(i);
}
indicatorPosition=newIndicatorPosition;
}
public void addTabView(Tab tab){
final TabView tabView=tab.view;
tabView.setSelected(false);
tabView.setActivated(false);
slidingTabIndicator.addView(tabView,tab.getPosition(),createLayoutParamsForTabs());
}
private LinearLayout.LayoutParams createLayoutParamsForTabs(){
final LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
updateTabViewLayoutParams(lp);
return lp;
}
private void updateTabViewLayoutParams(LinearLayout.LayoutParams lp){
if (mode == MODE_FIXED && tabGravity == GRAVITY_FILL) {
lp.width=0;
lp.weight=1;
}
 else {
lp.width=LinearLayout.LayoutParams.WRAP_CONTENT;
lp.weight=0;
}
}
protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
final int idealHeight=Math.round(com.ashera.widget.PluginInvoker.convertDpToPixel(getDefaultHeight() + "dp"));
switch (MeasureSpec.getMode(heightMeasureSpec)) {
case MeasureSpec.AT_MOST:
if (getChildCount() == 1 && MeasureSpec.getSize(heightMeasureSpec) >= idealHeight) {
getChildAt(0).setMinimumHeight(idealHeight);
}
break;
case MeasureSpec.UNSPECIFIED:
heightMeasureSpec=MeasureSpec.makeMeasureSpec(idealHeight + getPaddingTop() + getPaddingBottom(),MeasureSpec.EXACTLY);
break;
default :
break;
}
final int specWidth=MeasureSpec.getSize(widthMeasureSpec);
if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
tabMaxWidth=requestedTabMaxWidth > 0 ? requestedTabMaxWidth : (int)(specWidth - com.ashera.widget.PluginInvoker.convertDpToPixel(TAB_MIN_WIDTH_MARGIN + "dp"));
}
super.onMeasure(widthMeasureSpec,heightMeasureSpec);
if (getChildCount()-getChildTabItemCount() == 1) {
final View child=getChildAt(0);
boolean remeasure=false;
switch (mode) {
case MODE_AUTO:
case MODE_SCROLLABLE:
remeasure=child.getMeasuredWidth() < getMeasuredWidth();
break;
case MODE_FIXED:
remeasure=child.getMeasuredWidth() != getMeasuredWidth();
break;
}
if (remeasure) {
int childHeightMeasureSpec=getChildMeasureSpec(heightMeasureSpec,getPaddingTop() + getPaddingBottom(),child.getLayoutParams().height);
int childWidthMeasureSpec=MeasureSpec.makeMeasureSpec(getMeasuredWidth(),MeasureSpec.EXACTLY);
child.measure(childWidthMeasureSpec,childHeightMeasureSpec);
}
}
}
private void removeTabViewAt(int position){
final TabView view=(TabView)slidingTabIndicator.getChildAt(position);
slidingTabIndicator.removeViewAt(position);
if (view != null) {
view.reset();
tabViewPool.release(view);
}
requestLayout();
}
public void animateToTab(int newPosition){
if (newPosition == Tab.INVALID_POSITION) {
return;
}
if (/*getWindowToken() == null ||*/ !ViewCompat.isLaidOut(this) || slidingTabIndicator.childrenNeedLayout()) {
setScrollPosition(newPosition,0f,true);
return;
}
final int startScrollX=getScrollX();
final int targetScrollX=calculateScrollXForTab(newPosition,0);
if (startScrollX != targetScrollX) {
ensureScrollAnimator();
scrollAnimator.setIntValues(startScrollX,targetScrollX);
scrollAnimator.start();
}
slidingTabIndicator.animateIndicatorToPosition(newPosition,tabIndicatorAnimationDuration);
}
private void ensureScrollAnimator(){
if (scrollAnimator == null) {
scrollAnimator=new ValueAnimator();
scrollAnimator.setInterpolator(tabIndicatorTimeInterpolator);
scrollAnimator.setDuration(tabIndicatorAnimationDuration);
scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
public void onAnimationUpdate(ValueAnimator animator){
scrollTo((int)animator.getAnimatedValue(),0);
}
}
);
}
}
public void setSelectedTabView(int position){
final int tabCount=slidingTabIndicator.getChildCount();
if (position < tabCount) {
for (int i=0; i < tabCount; i++) {
final View child=slidingTabIndicator.getChildAt(i);
if ((i == position && !child.isSelected()) || (i != position && child.isSelected())) {
child.setSelected(i == position);
child.setActivated(i == position);
if (child instanceof TabView) {
((TabView)child).updateTab();
}
continue;
}
child.setSelected(i == position);
child.setActivated(i == position);
}
}
}
public void selectTab(Tab tab){
selectTab(tab,true);
}
public void selectTab(final Tab tab,boolean updateIndicator){
final Tab currentTab=selectedTab;
if (currentTab == tab) {
if (currentTab != null) {
dispatchTabReselected(tab);
animateToTab(tab.getPosition());
}
}
 else {
final int newPosition=tab != null ? tab.getPosition() : Tab.INVALID_POSITION;
if (updateIndicator) {
if ((currentTab == null || currentTab.getPosition() == Tab.INVALID_POSITION) && newPosition != Tab.INVALID_POSITION) {
setScrollPosition(newPosition,0f,true);
}
 else {
animateToTab(newPosition);
}
if (newPosition != Tab.INVALID_POSITION) {
setSelectedTabView(newPosition);
}
}
selectedTab=tab;
if (currentTab != null && currentTab.parent != null) {
dispatchTabUnselected(currentTab);
}
if (tab != null) {
dispatchTabSelected(tab);
}
}
}
private void dispatchTabSelected(final Tab tab){
for (int i=selectedListeners.size() - 1; i >= 0; i--) {
selectedListeners.get(i).onTabSelected(tab);
}
}
private void dispatchTabUnselected(final Tab tab){
for (int i=selectedListeners.size() - 1; i >= 0; i--) {
selectedListeners.get(i).onTabUnselected(tab);
}
}
private void dispatchTabReselected(final Tab tab){
for (int i=selectedListeners.size() - 1; i >= 0; i--) {
selectedListeners.get(i).onTabReselected(tab);
}
}
private int calculateScrollXForTab(int position,float positionOffset){
if (mode == MODE_SCROLLABLE || mode == MODE_AUTO) {
final View selectedChild=slidingTabIndicator.getChildAt(position);
if (selectedChild == null) {
return 0;
}
final View nextChild=position + 1 < slidingTabIndicator.getChildCount() ? slidingTabIndicator.getChildAt(position + 1) : null;
final int selectedWidth=selectedChild.getWidth();
final int nextWidth=nextChild != null ? nextChild.getWidth() : 0;
int scrollBase=selectedChild.getLeft() + (selectedWidth / 2) - (getWidth() / 2);
int scrollOffset=(int)((selectedWidth + nextWidth) * 0.5f * positionOffset);
return (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR) ? scrollBase + scrollOffset : scrollBase - scrollOffset;
}
return 0;
}
public void applyModeAndGravity(){
int paddingStart=0;
if (mode == MODE_SCROLLABLE || mode == MODE_AUTO) {
paddingStart=Math.max(0,contentInsetStart - tabPaddingStart);
}
ViewCompat.setPaddingRelative(slidingTabIndicator,paddingStart,0,0,0);
switch (mode) {
case MODE_AUTO:
case MODE_FIXED:
if (tabGravity == GRAVITY_START) {
Log.w(LOG_TAG,"GRAVITY_START is not supported with the current tab mode, GRAVITY_CENTER will be" + " used instead");
}
slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
break;
case MODE_SCROLLABLE:
applyGravityForModeScrollable(tabGravity);
break;
}
updateTabViews(true);
}
private void applyGravityForModeScrollable(int tabGravity){
switch (tabGravity) {
case GRAVITY_CENTER:
slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
break;
case GRAVITY_FILL:
Log.w(LOG_TAG,"MODE_SCROLLABLE + GRAVITY_FILL is not supported, GRAVITY_START will be used" + " instead");
case GRAVITY_START:
slidingTabIndicator.setGravity(GravityCompat.START);
break;
default :
break;
}
}
void updateTabViews(final boolean requestLayout){
for (int i=0; i < slidingTabIndicator.getChildCount(); i++) {
View child=slidingTabIndicator.getChildAt(i);
child.setMinimumWidth(getTabMinWidth());
updateTabViewLayoutParams((LinearLayout.LayoutParams)child.getLayoutParams());
if (requestLayout) {
child.requestLayout();
}
}
}
public static class Tab {
public static final int INVALID_POSITION=-1;
private Object tag;
private Drawable icon;
private CharSequence text;
private CharSequence contentDesc;
private int position=INVALID_POSITION;
private View customView;
private int labelVisibilityMode=TAB_LABEL_VISIBILITY_LABELED;
public TabLayout parent;
public TabView view;
private int id=NO_ID;
public Object getTag(){
return tag;
}
public Tab setTag(Object tag){
this.tag=tag;
return this;
}
public Tab setId(int id){
this.id=id;
if (view != null) {
view.setId(id);
}
return this;
}
public int getId(){
return id;
}
public View getCustomView(){
return customView;
}
public Tab setCustomView(View view){
customView=view;this.view.addClickListener(customView, false);
updateView();
return this;
}
public Drawable getIcon(){
return icon;
}
public int getPosition(){
return position;
}
void setPosition(int position){
this.position=position;
}
public CharSequence getText(){
return text;
}
public Tab setIcon(Drawable icon){
this.icon=icon;
if ((parent.tabGravity == GRAVITY_CENTER) || parent.mode == MODE_AUTO) {
parent.updateTabViews(true);
}
updateView();
if (BadgeUtils.USE_COMPAT_PARENT && view.hasBadgeDrawable() /*&& view.badgeDrawable.isVisible()*/) {
view.invalidate();
}
return this;
}
public Tab setText(CharSequence text){
if (TextUtils.isEmpty(contentDesc) && !TextUtils.isEmpty(text)) {
//view.//setContentDescription(text);
}
this.text=text;
updateView();
return this;
}
public BadgeDrawable getOrCreateBadge(){
return view.getOrCreateBadge();
}
public void removeBadge(){
view.removeBadge();
}
public BadgeDrawable getBadge(){
return view.getBadge();
}
public Tab setTabLabelVisibility(int mode){
this.labelVisibilityMode=mode;
if ((parent.tabGravity == GRAVITY_CENTER) || parent.mode == MODE_AUTO) {
parent.updateTabViews(true);
}
this.updateView();
if (BadgeUtils.USE_COMPAT_PARENT && view.hasBadgeDrawable() /*&& view.badgeDrawable.isVisible()*/) {
view.invalidate();
}
return this;
}
public int getTabLabelVisibility(){
return this.labelVisibilityMode;
}
public void select(){
if (parent == null) {
throw new IllegalArgumentException("Tab not attached to a TabLayout");
}
parent.selectTab(this);
}
public boolean isSelected(){
if (parent == null) {
throw new IllegalArgumentException("Tab not attached to a TabLayout");
}
int selectedPosition=parent.getSelectedTabPosition();
return selectedPosition != INVALID_POSITION && selectedPosition == position;
}
void updateView(){
if (view != null) {
view.update();
}
}
void reset(){
parent=null;
view=null;
tag=null;
icon=null;
id=NO_ID;
text=null;
contentDesc=null;
position=INVALID_POSITION;
customView=null;
}
}
public class TabView extends LinearLayout {
private Tab tab;
private TextView textView;
private ImageView iconView;
private View badgeAnchorView;
private BadgeDrawable badgeDrawable;
private View customView;
private TextView customTextView;
private ImageView customIconView;
private Drawable baseBackgroundDrawable;
private int defaultMaxLines=2;
public TabView(Context context){
super(context);
//updateBackgroundDrawable(getContext());
ViewCompat.setPaddingRelative(this,tabPaddingStart,tabPaddingTop,tabPaddingEnd,tabPaddingBottom);
setGravity(Gravity.CENTER);
setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
setClickable(true);
}public void setBaseBackgroundDrawable(Drawable baseBackgroundDrawable) {this.baseBackgroundDrawable = baseBackgroundDrawable;tabBackgroundResId=baseBackgroundDrawable==null?0:1;updateBackgroundDrawable(getContext());}public void initTabView() {addClickListener(this, true);} public void addClickListener(View view, boolean allPlatforms) {if (!allPlatforms && !com.ashera.widget.PluginInvoker.getOS().equalsIgnoreCase("swt")) {return;}View.OnClickListener onClickListener = new View.OnClickListener() {public void onClick(View v) {performClick();}};view.setMyAttribute("onClick", onClickListener);if (view instanceof ViewGroup) {LayoutInflater.recurseSet((ViewGroup) view, onClickListener);}//ViewCompat.setPointerIcon(this,PointerIconCompat.getSystemIcon(getContext(),PointerIconCompat.TYPE_HAND));
}
private void updateBackgroundDrawable(Context context){
if (tabBackgroundResId != 0) {
//baseBackgroundDrawable=AppCompatResources.getDrawable(context,tabBackgroundResId);
if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
baseBackgroundDrawable.setState(getDrawableState());
}
}
 else {
baseBackgroundDrawable=null;
}
Drawable background;
//Drawable contentDrawable=new GradientDrawable();
//((GradientDrawable)contentDrawable).setColor(Color.TRANSPARENT);
if (false/*tabRippleColorStateList != null*/) {
//GradientDrawable maskDrawable=new GradientDrawable();
//maskDrawable.setCornerRadius(0.00001F);
//maskDrawable.setColor(Color.WHITE);
//ColorStateList rippleColor=RippleUtils.convertToRippleDrawableColor(tabRippleColorStateList);
if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
//background=new RippleDrawable(rippleColor,unboundedRipple ? null : contentDrawable,unboundedRipple ? null : maskDrawable);
}
 else {
//Drawable rippleDrawable=DrawableCompat.wrap(maskDrawable);
iconView.setMyAttribute("tint", tabIconTint);//DrawableCompat.setTintList(rippleDrawable,rippleColor);
//background=new LayerDrawable(new Drawable[]{contentDrawable,rippleDrawable});
}
}
 else {
//background=contentDrawable;
}
setMyAttribute("background", baseBackgroundDrawable);//ViewCompat.setBackground(this,background);
TabLayout.this.invalidate();
}
protected void drawableStateChanged(){
super.drawableStateChanged();
boolean changed=false;
int[] state=getDrawableState();
if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
changed|=baseBackgroundDrawable.setState(state);
}
if (changed) {
invalidate();
TabLayout.this.invalidate();
}
}
public boolean performClick(){
final boolean handled=false/*super.performClick()*/;
if (tab != null) {
if (!handled) {
//playSoundEffect(SoundEffectConstants.CLICK);
}
tab.select();
return true;
}
 else {
return handled;
}
}
public void setSelected(final boolean selected){
final boolean changed=isSelected() != selected;
super.setSelected(selected);
if (changed && selected && Build.VERSION.SDK_INT < 16) {
//sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
}
if (textView != null) {
textView.setSelected(selected);
}
if (iconView != null) {
iconView.setSelected(selected);
}
if (customView != null) {
customView.setSelected(selected);
}
}
public void onMeasure(final int origWidthMeasureSpec,final int origHeightMeasureSpec){
final int specWidthSize=MeasureSpec.getSize(origWidthMeasureSpec);
final int specWidthMode=MeasureSpec.getMode(origWidthMeasureSpec);
final int maxWidth=getTabMaxWidth();
final int widthMeasureSpec;
final int heightMeasureSpec=origHeightMeasureSpec;
if (maxWidth > 0 && (specWidthMode == MeasureSpec.UNSPECIFIED || specWidthSize > maxWidth)) {
widthMeasureSpec=MeasureSpec.makeMeasureSpec(tabMaxWidth,MeasureSpec.AT_MOST);
}
 else {
widthMeasureSpec=origWidthMeasureSpec;
}
super.onMeasure(widthMeasureSpec,heightMeasureSpec);
if (textView != null) {
float textSize=tabTextSize;
int maxLines=defaultMaxLines;
if (iconView != null && iconView.getVisibility() == VISIBLE) {
maxLines=1;
}
 else if (textView != null && TextViewCompat.getLineCount(textView) > 1) {
textSize=tabTextMultiLineSize;
}
final float curTextSize=TextViewCompat.getTextSize(textView);
final int curLineCount=TextViewCompat.getLineCount(textView);
final int curMaxLines=TextViewCompat.getMaxLines(textView);
if (textSize != curTextSize || (curMaxLines >= 0 && maxLines != curMaxLines)) {
boolean updateTextView=true;
if (mode == MODE_FIXED && textSize > curTextSize && curLineCount == 1) {
final Layout layout=TextViewCompat.getLayout(textView);
if (layout == null || approximateLineWidth(layout,0,textSize) > getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) {
updateTextView=false;
}
}
if (updateTextView) {
TextViewCompat.setTextSize(textView,textSize);
textView.setMaxLines(maxLines);
super.onMeasure(widthMeasureSpec,heightMeasureSpec);
}
}
}
}
void setTab(final Tab tab){
if (tab != this.tab) {
this.tab=tab;
update();
}
}
void reset(){
setTab(null);
setSelected(false);
}
final void updateTab(){
final Tab tab=this.tab;
final View custom=tab != null ? tab.getCustomView() : null;
if (custom != null) {
final ViewParent customParent=custom.getParent();
if (customParent != this) {
if (customParent != null) {
((ViewGroup)customParent).removeView(custom);
}
if (customView != null) {
final ViewParent customViewParent=customView.getParent();
if (customViewParent != null) {
((ViewGroup)customViewParent).removeView(customView);
}
}
addView(custom);
}
customView=custom;
if (this.textView != null) {
this.textView.setVisibility(GONE);
}
if (this.iconView != null) {
this.iconView.setVisibility(GONE);
this.iconView.setImageDrawable(null);
}
customTextView=custom.findViewById(com.ashera.widget.IdGenerator.getId("@+id/text1"));
if (customTextView != null) {
defaultMaxLines=TextViewCompat.getMaxLines(customTextView);
}
customIconView=custom.findViewById(com.ashera.widget.IdGenerator.getId("@+id/icon"));
}
 else {
if (customView != null) {
removeView(customView);
customView=null;
}
customTextView=null;
customIconView=null;
}
if (customView == null) {
if (this.iconView == null) {
inflateAndAddDefaultIconView();
}
if (this.textView == null) {
inflateAndAddDefaultTextView();
defaultMaxLines=TextViewCompat.getMaxLines(this.textView);
}
TextViewCompat.setTextAppearance(this.textView,defaultTabTextAppearance);
if (isSelected() && selectedTabTextAppearance != null) {
TextViewCompat.setTextAppearance(this.textView,selectedTabTextAppearance);
}
 else {
TextViewCompat.setTextAppearance(this.textView,tabTextAppearance);
}
if (tabTextColors != null) {
this.textView.setMyAttribute("textColor", tabTextColors);
}
updateTextAndIcon(this.textView,this.iconView,true);
tryUpdateBadgeAnchor();
addOnLayoutChangeListener(iconView);
addOnLayoutChangeListener(textView);
}
 else {
if (customTextView != null || customIconView != null) {
updateTextAndIcon(customTextView,customIconView,false);
}
}
if (/*tab != null && !TextUtils.isEmpty(tab.contentDesc)*/false) {
//setContentDescription(tab.contentDesc);
}
}
final void update(){
updateTab();
setSelected(this.tab != null && this.tab.isSelected());
}
private void inflateAndAddDefaultIconView(){
ViewGroup iconViewParent=this;
if (BadgeUtils.USE_COMPAT_PARENT) {
iconViewParent=createPreApi18BadgeAnchorRoot();
addView(iconViewParent,0);
}
this.iconView=(ImageView)LayoutInflater.from(getContext()).inflate("@layout/design_layout_tab_icon_new",iconViewParent,false);addClickListener(this.iconView, false);
iconViewParent.addView(iconView,0);
}
private void inflateAndAddDefaultTextView(){
ViewGroup textViewParent=this;
if (BadgeUtils.USE_COMPAT_PARENT) {
textViewParent=createPreApi18BadgeAnchorRoot();
addView(textViewParent);
}
this.textView=(TextView)LayoutInflater.from(getContext()).inflate("@layout/design_layout_tab_text_new",textViewParent,false);addClickListener(this.textView, false);
textViewParent.addView(textView);
}
private FrameLayout createPreApi18BadgeAnchorRoot(){
FrameLayout frameLayout=new FrameLayout(/*getContext()*/);
FrameLayout.LayoutParams layoutparams=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
frameLayout.setLayoutParams(layoutparams);
return frameLayout;
}
private BadgeDrawable getOrCreateBadge(){
if (badgeDrawable == null) {
badgeDrawable=BadgeDrawable.create(getContext());
}
tryUpdateBadgeAnchor();
if (badgeDrawable == null) {
throw new IllegalStateException("Unable to create badge");
}
return badgeDrawable;
}
private BadgeDrawable getBadge(){
return badgeDrawable;
}
private void removeBadge(){
if (badgeAnchorView != null) {
tryRemoveBadgeFromAnchor();
}
badgeDrawable=null;
}
private void addOnLayoutChangeListener(final View view){
if (view == null) {
return;
}
view.addOnLayoutChangeListener(new OnLayoutChangeListener(){
public void onLayoutChange(View v,int left,int top,int right,int bottom,int oldLeft,int oldTop,int oldRight,int oldBottom){
if (view.getVisibility() == VISIBLE) {
tryUpdateBadgeDrawableBounds(view);
}
}
}
);
}
private void tryUpdateBadgeAnchor(){
if (!hasBadgeDrawable()) {
return;
}
if (customView != null) {
tryRemoveBadgeFromAnchor();
}
 else {
if (iconView != null && tab != null && tab.getIcon() != null) {
if (badgeAnchorView != iconView) {
tryRemoveBadgeFromAnchor();
tryAttachBadgeToAnchor(iconView);
}
 else {
tryUpdateBadgeDrawableBounds(iconView);
}
}
 else if (textView != null && tab != null && tab.getTabLabelVisibility() == TAB_LABEL_VISIBILITY_LABELED) {
if (badgeAnchorView != textView) {
tryRemoveBadgeFromAnchor();
tryAttachBadgeToAnchor(textView);
}
 else {
tryUpdateBadgeDrawableBounds(textView);
}
}
 else {
tryRemoveBadgeFromAnchor();
}
}
}
private void tryAttachBadgeToAnchor(View anchorView){
if (!hasBadgeDrawable()) {
return;
}
if (anchorView != null) {
clipViewToPaddingForBadge(false);
BadgeUtils.attachBadgeDrawable(badgeDrawable,anchorView,getCustomParentForBadge(anchorView));
badgeAnchorView=anchorView;
}
}
private void tryRemoveBadgeFromAnchor(){
if (!hasBadgeDrawable()) {
return;
}
clipViewToPaddingForBadge(true);
if (badgeAnchorView != null) {
BadgeUtils.detachBadgeDrawable(badgeDrawable,badgeAnchorView);
badgeAnchorView=null;
}
}
private void clipViewToPaddingForBadge(boolean flag){
setMyAttribute("clipChildren",flag);
setClipToPadding(flag);
ViewGroup parent=(ViewGroup)getParent();
if (parent != null) {
parent.setMyAttribute("clipChildren",flag);
parent.setClipToPadding(flag);
}
}
final void updateOrientation(){
setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
if (customTextView != null || customIconView != null) {
updateTextAndIcon(customTextView,customIconView,false);
}
 else {
updateTextAndIcon(textView,iconView,true);
}
}
private void updateTextAndIcon(final TextView textView,final ImageView iconView,final boolean addDefaultMargins){
final Drawable icon=(tab != null && tab.getIcon() != null) ? tab.getIcon() : null;
if (icon != null) {
iconView.setMyAttribute("tint", tabIconTint);//DrawableCompat.setTintList(icon,tabIconTint);
if (false/*tabIconTintMode != null*/) {
//DrawableCompat.setTintMode(icon,tabIconTintMode);
}
}
final CharSequence text=tab != null ? tab.getText() : null;
if (iconView != null) {
if (icon != null) {
iconView.setMyAttribute("src", icon);
iconView.setVisibility(VISIBLE);
setVisibility(VISIBLE);
}
 else {
iconView.setVisibility(GONE);
iconView.setImageDrawable(null);
}
}
final boolean hasText=!TextUtils.isEmpty(text);
final boolean showingText;
if (textView != null) {
showingText=hasText && tab.labelVisibilityMode == TAB_LABEL_VISIBILITY_LABELED;
textView.setMyAttribute("text",hasText ? text : null);
textView.setVisibility(showingText ? VISIBLE : GONE);
if (hasText) {
setVisibility(VISIBLE);
}
}
 else {
showingText=false;
}
if (addDefaultMargins && iconView != null) {
MarginLayoutParams lp=((MarginLayoutParams)iconView.getLayoutParams());
int iconMargin=0;
if (showingText && iconView.getVisibility() == VISIBLE) {
iconMargin=(int)com.ashera.widget.PluginInvoker.convertDpToPixel(DEFAULT_GAP_TEXT_ICON + "dp");
}
if (inlineLabel) {
if (iconMargin != MarginLayoutParamsCompat.getMarginEnd(lp)) {
MarginLayoutParamsCompat.setMarginEnd(lp,iconMargin);
lp.bottomMargin=0;
iconView.setLayoutParams(lp);
iconView.requestLayout();
}
}
 else {
if (iconMargin != lp.bottomMargin) {
lp.bottomMargin=iconMargin;
MarginLayoutParamsCompat.setMarginEnd(lp,0);
iconView.setLayoutParams(lp);
iconView.requestLayout();
}
}
}
final CharSequence contentDesc=tab != null ? tab.contentDesc : null;
if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP || VERSION.SDK_INT > VERSION_CODES.M) {
//TooltipCompat.setTooltipText(this,hasText ? text : contentDesc);
}
}
private void tryUpdateBadgeDrawableBounds(View anchor){
if (hasBadgeDrawable() && anchor == badgeAnchorView) {
BadgeUtils.setBadgeDrawableBounds(badgeDrawable,anchor,getCustomParentForBadge(anchor));
}
}
private boolean hasBadgeDrawable(){
return badgeDrawable != null;
}
private FrameLayout getCustomParentForBadge(View anchor){
if (anchor != iconView && anchor != textView) {
return null;
}
return BadgeUtils.USE_COMPAT_PARENT ? ((FrameLayout)anchor.getParent()) : null;
}
int getContentWidth(){
boolean initialized=false;
int left=0;
int right=0;
for (View view : new View[]{textView,iconView,customView}) {
if (view != null && view.getVisibility() == View.VISIBLE) {
left=initialized ? Math.min(left,view.getLeft()) : view.getLeft();
right=initialized ? Math.max(right,view.getRight()) : view.getRight();
initialized=true;
}
}
return right - left;
}
int getContentHeight(){
boolean initialized=false;
int top=0;
int bottom=0;
for (View view : new View[]{textView,iconView,customView}) {
if (view != null && view.getVisibility() == View.VISIBLE) {
top=initialized ? Math.min(top,view.getTop()) : view.getTop();
bottom=initialized ? Math.max(bottom,view.getBottom()) : view.getBottom();
initialized=true;
}
}
return bottom - top;
}
public Tab getTab(){
return tab;
}
private float approximateLineWidth(Layout layout,int line,float textSize){
return layout.getLineWidth(line) * (textSize / layout.getPaint().getTextSize());
}
}
public class SlidingTabIndicator extends LinearLayout {
ValueAnimator indicatorAnimator;
private int layoutDirection=-1;
public SlidingTabIndicator(Context context){
super(context);
setWillNotDraw(false);
}
void setSelectedIndicatorHeight(int height){
Rect bounds=tabSelectedIndicator.getBounds();
tabSelectedIndicator.setBounds(bounds.left,0,bounds.right,height);
this.requestLayout();
}
boolean childrenNeedLayout(){
for (int i=0, z=getChildCount(); i < z; i++) {
final View child=getChildAt(i);
if (child.getWidth() <= 0) {
return true;
}
}
return false;
}
void setIndicatorPositionFromTabPosition(int position,float positionOffset){
indicatorPosition=Math.round(position + positionOffset);
if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
indicatorAnimator.cancel();
}
final View firstTitle=getChildAt(position);
final View nextTitle=getChildAt(position + 1);
tweenIndicatorPosition(firstTitle,nextTitle,positionOffset);
}
public void onRtlPropertiesChanged(int layoutDirection){
super.onRtlPropertiesChanged(layoutDirection);
if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
if (this.layoutDirection != layoutDirection) {
requestLayout();
this.layoutDirection=layoutDirection;
}
}
}
protected void onMeasure(final int widthMeasureSpec,final int heightMeasureSpec){
super.onMeasure(widthMeasureSpec,heightMeasureSpec);
if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
return;
}
if ((tabGravity == GRAVITY_CENTER) || mode == MODE_AUTO) {
final int count=getChildCount();
int largestTabWidth=0;
for (int i=0, z=count; i < z; i++) {
View child=getChildAt(i);
if (child.getVisibility() == VISIBLE) {
largestTabWidth=Math.max(largestTabWidth,child.getMeasuredWidth());
}
}
if (largestTabWidth <= 0) {
return;
}
final int gutter=(int)com.ashera.widget.PluginInvoker.convertDpToPixel(FIXED_WRAP_GUTTER_MIN + "dp");
boolean remeasure=false;
if (largestTabWidth * count <= getMeasuredWidth() - gutter * 2) {
for (int i=0; i < count; i++) {
final LinearLayout.LayoutParams lp=(LayoutParams)getChildAt(i).getLayoutParams();
if (lp.width != largestTabWidth || lp.weight != 0) {
lp.width=largestTabWidth;
lp.weight=0;
remeasure=true;
}
}
}
 else {
tabGravity=GRAVITY_FILL;
updateTabViews(false);
remeasure=true;
}
if (remeasure) {
super.onMeasure(widthMeasureSpec,heightMeasureSpec);
}
}
}
protected void onLayout(boolean changed,int l,int t,int r,int b){
super.onLayout(changed,l,t,r,b);
if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
updateOrRecreateIndicatorAnimation(false,getSelectedTabPosition(),-1);
}
 else {
jumpIndicatorToIndicatorPosition();
}
}
private void jumpIndicatorToPosition(int position){
if (viewPagerScrollState != SCROLL_STATE_IDLE && !(getTabSelectedIndicator().getBounds().left == -1 && getTabSelectedIndicator().getBounds().right == -1)) {
return;
}
final View currentView=getChildAt(position);
tabIndicatorInterpolator.setIndicatorBoundsForTab(TabLayout.this,currentView,tabSelectedIndicator);
indicatorPosition=position;
}
private void jumpIndicatorToSelectedPosition(){
jumpIndicatorToPosition(getSelectedTabPosition());
}
private void jumpIndicatorToIndicatorPosition(){
if (indicatorPosition == -1) {
indicatorPosition=getSelectedTabPosition();
}
jumpIndicatorToPosition(indicatorPosition);
}
private void tweenIndicatorPosition(View startTitle,View endTitle,float fraction){
boolean hasVisibleTitle=startTitle != null && startTitle.getWidth() > 0;
if (hasVisibleTitle) {
tabIndicatorInterpolator.updateIndicatorForOffset(TabLayout.this,startTitle,endTitle,fraction,tabSelectedIndicator);
}
 else {
tabSelectedIndicator.setBounds(-1,tabSelectedIndicator.getBounds().top,-1,tabSelectedIndicator.getBounds().bottom);
}
//ViewCompat.postInvalidateOnAnimation(this);
}
void animateIndicatorToPosition(final int position,int duration){
if (indicatorAnimator != null && indicatorAnimator.isRunning() && indicatorPosition != position) {
indicatorAnimator.cancel();
}
updateOrRecreateIndicatorAnimation(true,position,duration);
}
private void updateOrRecreateIndicatorAnimation(boolean recreateAnimation,final int position,int duration){
if (indicatorPosition == position) {
return;
}
final View currentView=getChildAt(getSelectedTabPosition());
final View targetView=getChildAt(position);
if (targetView == null) {
jumpIndicatorToSelectedPosition();
return;
}
indicatorPosition=position;
ValueAnimator.AnimatorUpdateListener updateListener=new ValueAnimator.AnimatorUpdateListener(){
public void onAnimationUpdate(ValueAnimator valueAnimator){
tweenIndicatorPosition(currentView,targetView,valueAnimator.getAnimatedFraction());
}
}
;
if (recreateAnimation) {
ValueAnimator animator=indicatorAnimator=new ValueAnimator();
animator.setInterpolator(tabIndicatorTimeInterpolator);
animator.setDuration(duration);
animator.setFloatValues(0F,1F);
animator.addUpdateListener(updateListener);
animator.start();
}
 else {
indicatorAnimator.removeAllUpdateListeners();
indicatorAnimator.addUpdateListener(updateListener);
}
}
public void onDraw(Canvas canvas){
int indicatorHeight=tabSelectedIndicator.getBounds().height();
if (indicatorHeight < 0) {
indicatorHeight=tabSelectedIndicator.getIntrinsicHeight();
}
int indicatorTop=0;
int indicatorBottom=0;
switch (tabIndicatorGravity) {
case INDICATOR_GRAVITY_BOTTOM:
indicatorTop=getHeight() - indicatorHeight;
indicatorBottom=getHeight();
break;
case INDICATOR_GRAVITY_CENTER:
indicatorTop=(getHeight() - indicatorHeight) / 2;
indicatorBottom=(getHeight() + indicatorHeight) / 2;
break;
case INDICATOR_GRAVITY_TOP:
indicatorTop=0;
indicatorBottom=indicatorHeight;
break;
case INDICATOR_GRAVITY_STRETCH:
indicatorTop=0;
indicatorBottom=getHeight();
break;
default :
break;
}
if (tabSelectedIndicator.getBounds().width() > 0) {
Rect indicatorBounds=tabSelectedIndicator.getBounds();
tabSelectedIndicator.setBounds(indicatorBounds.left,indicatorTop,indicatorBounds.right,indicatorBottom);
tabSelectedIndicator.draw(canvas);
}
super.onDraw(canvas);
}
}
private static ColorStateList createColorStateList(int defaultColor,int selectedColor){
final int[][] states=new int[2][];
final int[] colors=new int[2];
int i=0;
states[i]=SELECTED_STATE_SET;
colors[i]=selectedColor;
i++;
states[i]=EMPTY_STATE_SET;
colors[i]=defaultColor;
i++;
return new ColorStateList(states,colors);
}
private int getDefaultHeight(){
boolean hasIconAndText=false;
for (int i=0, count=tabs.size(); i < count; i++) {
Tab tab=tabs.get(i);
if (tab != null && tab.getIcon() != null && !TextUtils.isEmpty(tab.getText())) {
hasIconAndText=true;
break;
}
}
return (hasIconAndText && !inlineLabel) ? DEFAULT_HEIGHT_WITH_TEXT_ICON : DEFAULT_HEIGHT;
}
private int getTabMinWidth(){
if (requestedTabMinWidth != INVALID_WIDTH) {
return requestedTabMinWidth;
}
return (mode == MODE_SCROLLABLE || mode == MODE_AUTO) ? scrollableTabMinWidth : 0;
}
int getTabMaxWidth(){
return tabMaxWidth;
}
public static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
private final WeakReference<TabLayout> tabLayoutRef;
private int previousScrollState;
private int scrollState;
public TabLayoutOnPageChangeListener(TabLayout tabLayout){
tabLayoutRef=new WeakReference<>(tabLayout);
}
public void onPageScrollStateChanged(final int state){
previousScrollState=scrollState;
scrollState=state;
TabLayout tabLayout=tabLayoutRef.get();
if (tabLayout != null) {
tabLayout.updateViewPagerScrollState(scrollState);
}
}
public void onPageScrolled(final int position,final float positionOffset,final int positionOffsetPixels){
final TabLayout tabLayout=tabLayoutRef.get();
if (tabLayout != null) {
final boolean updateSelectedTabView=scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING;
final boolean updateIndicator=!(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
tabLayout.setScrollPosition(position,positionOffset,updateSelectedTabView,updateIndicator,false);
}
}
public void onPageSelected(final int position){
final TabLayout tabLayout=tabLayoutRef.get();
if (tabLayout != null && tabLayout.getSelectedTabPosition() != position && position < tabLayout.getTabCount()) {
final boolean updateIndicator=scrollState == SCROLL_STATE_IDLE || (scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
tabLayout.selectTab(tabLayout.getTabAt(position),updateIndicator);
}
}
void reset(){
previousScrollState=scrollState=SCROLL_STATE_IDLE;
}
}
public static class ViewPagerOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
private final ViewPager viewPager;
public ViewPagerOnTabSelectedListener(ViewPager viewPager){
this.viewPager=viewPager;
}
public void onTabSelected(TabLayout.Tab tab){
viewPager.setCurrentItem(tab.getPosition());
}
public void onTabUnselected(TabLayout.Tab tab){
}
public void onTabReselected(TabLayout.Tab tab){
}
}
private class PagerAdapterObserver extends DataSetObserver {
PagerAdapterObserver(){
}
public void onChanged(){
populateFromPagerAdapter();
}
public void onInvalidated(){
populateFromPagerAdapter();
}
}
private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
private boolean autoRefresh;
AdapterChangeListener(){
}
public void onAdapterChanged(ViewPager viewPager,PagerAdapter oldAdapter,PagerAdapter newAdapter){
if (TabLayout.this.viewPager == viewPager) {
setPagerAdapter(newAdapter,autoRefresh);
}
}
void setAutoRefresh(boolean autoRefresh){
this.autoRefresh=autoRefresh;
}
}
@Override public int getScrollX(){
int scrollX=((Number)((com.ashera.widget.ILifeCycleDecorator)this).getWidget().getAttribute("scrollX",true)).intValue();
return scrollX;
}
public ColorStateList getTabIconTint(){
return tabIconTint;
}
public void setTabTextAppearance(String tabTextAppearance){
this.tabTextAppearance=tabTextAppearance;
}
public void setRequestedTabMinWidth(int requestedTabMinWidth){
this.requestedTabMinWidth=requestedTabMinWidth;
}
public void setRequestedTabMaxWidth(int requestedTabMaxWidth){
this.requestedTabMaxWidth=requestedTabMaxWidth;
}
public void setContentInsetStart(int contentInsetStart){
this.contentInsetStart=contentInsetStart;
}
public int getTabPaddingStart(){
return tabPaddingStart;
}
public void setTabPaddingStart(int tabPaddingStart){
this.tabPaddingStart=tabPaddingStart;
}
public int getTabPaddingTop(){
return tabPaddingTop;
}
public void setTabPaddingTop(int tabPaddingTop){
this.tabPaddingTop=tabPaddingTop;
}
public int getTabPaddingEnd(){
return tabPaddingEnd;
}
public void setTabPaddingEnd(int tabPaddingEnd){
this.tabPaddingEnd=tabPaddingEnd;
}
public int getTabPaddingBottom(){
return tabPaddingBottom;
}
public void setTabPaddingBottom(int tabPaddingBottom){
this.tabPaddingBottom=tabPaddingBottom;
}
private final static int tintColor=Color.parseColor("#0000ff");
private int getChildTabItemCount(){
int count=0;
for (int i=0; i < getChildCount(); i++) {
View view=getChildAt(i);
if (view instanceof com.ashera.model.IViewStub) {
count++;
}
}
return count;
}
public void initTabLayout(){
slidingTabIndicator=(SlidingTabIndicator)LayoutInflater.from(mContext).inflate("@layout/design_sliding_tab_indicator",this,true);
setTabTextColors(Color.BLACK,tintColor);
tabIconTint=createColorStateList(Color.BLACK,tintColor);
setSelectedTabIndicatorHeight((int)com.ashera.widget.PluginInvoker.convertDpToPixel("4dp"));
applyModeAndGravity();
}
public TabLayout(){
scrollableTabMinWidth=0;
requestedTabMaxWidth=Integer.MAX_VALUE;
requestedTabMinWidth=0;
tabBackgroundResId=0;
tabTextAppearance=null;
defaultTabTextAppearance=null;
tabIndicatorTimeInterpolator=new androidx.interpolator.view.animation.FastOutLinearInInterpolator();
tabIndicatorInterpolator=new TabIndicatorInterpolator();
tabSelectedIndicator=new Drawable();
tabSelectedIndicator.setDrawable(tintColor);
mode=MODE_FIXED;
tabPaddingStart=tabPaddingEnd=(int)com.ashera.widget.PluginInvoker.convertDpToPixel("12dp");
requestedTabMinWidth=(int)com.ashera.widget.PluginInvoker.convertDpToPixel("60dp");
tabIndicatorFullWidth=true;
tabIndicatorAnimationDuration=ANIMATION_DURATION;
}
@Override public void requestLayout(){
super.requestLayout();
for (int i=0; i < getTabCount(); i++) {
Tab tab=getTabAt(i);
if (tab != null && tab.view != null) {
if (tab.view.iconView != null) {
tab.view.iconView.requestLayout();
}
if (tab.view.textView != null) {
tab.view.textView.requestLayout();
}
if (tab.view.customView != null) {
tab.view.customView.requestLayout();
}
}
}
}
static class TextViewCompat {
public static int getMaxLines(TextView textView){
return 0;
}
public static void setTextAppearance(TextView textView,String textAppearance){
if (textAppearance != null) {
textView.setMyAttribute("textAppearance",textAppearance);
}
}
public static int getLineCount(TextView textView){
return 1;
}
public static int getTextSize(TextView textView){
return 1;
}
public static Layout getLayout(TextView textView){
return null;
}
public static void setTextSize(TextView textView,float textSize){
}
}
static class Paint {
public float getTextSize(){
return 0;
}
}
static class Layout {
public int getLineWidth(int line){
return 0;
}
public Paint getPaint(){
return null;
}
}
}
