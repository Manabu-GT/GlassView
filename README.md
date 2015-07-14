GlassView
===========

GlassView is an Android library that allows developers to easily add a glass-like effect inspired by a 
[GlassActionBar][1].

GlassView extends a RelativeLayout and it blurs the parent view behind it using the [ScriptIntrinsicBlur][2]
from v8 Support Library "[android.support.v8.renderscript][3]" package.

<img src="https://raw.github.com/Manabu-GT/GlassView/master/art/readme_demo.gif" width=246 height=421 alt="Quick Demo">

Try out the sample application:

<a href="https://play.google.com/store/apps/details?id=com.ms.square.android.glassviewsample">
  <img alt="Android app on Google Play"
       src="https://developer.android.com/images/brand/en_app_rgb_wo_45.png" />
</a>

Requirements
-------------
API Level 8 (Froyo) and above.

Setup
------
You just need to add the followings to your ***build.gradle*** file:

```
repositories {
    maven { url 'http://Manabu-GT.github.com/GlassView/mvn-repo' }
}

dependencies {
    compile 'com.ms.square:glassview:0.1.0'
}

android {
    defaultConfig {
        renderscriptTargetApi 21
        renderscriptSupportModeEnabled true
    }
}
```

Usage
------
Using the library is really simple, just look at the source code of the [provided sample][4].

Also, you can optionally set the following attributes in your layout xml file to customize the behavior
of the GlassView.

 * `downSampling` (defaults to 3)
 The value used to downscale bitmap for faster blur processing. For example, if this value is 
 set to 2, the 1/4 size of the original view will be used to apply the blur effect.
 This is important because as the view gets bigger, the more pixels need to be blurred and it significantly
 affects the speed of the blur processing.

 * `blurRadius` (defaults to 5)
 Radius of blur effect (allowed values are between 0 and 25)

```
  <!-- sample xml -->
  <merge xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:glassview="http://schemas.android.com/apk/res-auto"
      android:layout_width="match_parent"
      android:layout_height="match_parent">
      
      <ScrollView
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:scrollbars="none">
          <FrameLayout
              android:layout_width="match_parent"
              android:layout_height="wrap_content">
              <ImageView
                  android:id="@+id/bg_img"
                  android:layout_width="match_parent"
                  android:layout_height="wrap_content"
                  android:src="@drawable/lolipop_bg"/>
          </FrameLayout>
      </ScrollView>
  
      <com.ms.square.android.glassview.GlassView
          android:id="@+id/glass_view"
          android:layout_width="match_parent"
          android:layout_height="200dp"
          android:layout_gravity="bottom"
          android:background="#55ffffff"
          glassview:blurRadius="10">
            <!-- Child views could go here -->
      </com.ms.square.android.glassview.GlassView>
      
  </merge>
```

License
----------

    Copyright 2014 Manabu Shimobe

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[1]: https://github.com/ManuelPeinado/GlassActionBar
[2]: http://developer.android.com/reference/android/renderscript/ScriptIntrinsicBlur.html
[3]: http://developer.android.com/reference/android/support/v8/renderscript/package-summary.html
[4]: https://github.com/Manabu-GT/GlassView/tree/master/sample
