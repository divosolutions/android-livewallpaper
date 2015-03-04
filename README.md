# Android Live-wallpaper

## creating a wallpaper
This project can be used as a template/engine for creating android live-wallpapers like the included TestWallpaper,
perhaps with better graphics ;) .

Besides the fact, that you have to create a new module in the android studio project, give it a meaningful name and
include the PuvoWallpaperBase module (or to whatever you want to rename it) as a dependency there are some basic steps
how to create you own wallpaper.

### names of the images
This engine uses the names of the images/sprites in the resources to get information about them

* the name and an id
* an indicator if it is a "special" resource
* where to place the sprite on the screen (position, layer and number in layer)
* the number of frames (if it is a sprite sheet) and the frames per second rate

A full description can be found in [Defines.java] (https://github.com/divosolutions/android-livewallpaper/blob/master/android_studio_project/PuvoWallpaperBase/src/main/java/com/puvo/livewallpapers/puvowallpaperbase/Defines.java)
above the `getAllResourceData` function.

As long as a sprite is not marked as special, it will create a
[BaseObject] (https://github.com/divosolutions/android-livewallpaper/blob/master/android_studio_project/PuvoWallpaperBase/src/main/java/com/puvo/livewallpapers/puvowallpaperbase/BaseObject.java)
at the specified position/layer/order. Otherwise a special object (e.g. Particle) derived from BaseObject will be
created.

### layers and there parallax
IIf you have more than one layer, you might want to have a parallax (pseudo 3D) effect like the layers in the back move
slower the the one on thr front. You can specify that in the customValues.xml file in the *virtual_scroll_speed_factor*
string array. The index of an entries is the layer's number and the value is a factor. A value of 1.5 means that if the 
normal background is 1000 pixels, this layer is 1500 pixels in width.

### animations and multi-frame images
There are two ways of creating animations. One is a sprite sheet and the other the definition of several sprites.
What they have in common is, that all sprite-frames must have the same size. 

A sprite sheet is like a film strip. All frames must be aligned. The name of such a sheet could be
*r3__0__character_bk__4__1__14__20__405__540__m138__1910.png*. This would mean, that the character BK, which will be
positioned at (x=405,y=540) in layer 3 with number 0, is a special sprite
with the id 4 and has 14 frames which are running with 20 fps. The left border is -138 and the right 1910.

Sometimes a sprite sheet can get very large (especially in the x dimension) and android will get an `OutOfMemoryError`
because of that. For this case you can use the other way, like it is done in the 
[TestWallpaper] (https://github.com/divosolutions/android-livewallpaper/tree/master/android_studio_project/TestWallpaper/src/main/res/drawable-nodpi).
The first sprite's name will hold all the relevant information and the other sprites only offer the frame number:

* r2__2__character_a__7__1__1__1__900__200__m496__2000.png: frame 1, layer 2, number 2, id 7, special, 1 fps, (x=900, y=200), -496 <-> 2000
* r2__2__character_a__7__1__1__0__0__0__0__1.png: frame 2
* r2__2__character_a__7__1__1__0__0__0__0__2.png: frame 3
* r2__2__character_a__7__1__1__0__0__0__0__3.png: frame 4
* r2__2__character_a__7__1__1__0__0__0__0__4.png: frame 5
* r2__2__character_a__7__1__1__0__0__0__0__5.png: frame 6
* r2__2__character_a__7__1__1__0__0__0__0__6.png: frame 7

Important is, that the number value of all sprites are the same (in this case 2) and the fps value of the frames 2-...
are 0.

### preferences
Some of the preferences depend on the name of a sprite. I will not (for now) explain everything. Instead I will explain
it for the [Visit] (https://github.com/divosolutions/android-livewallpaper/blob/master/android_studio_project/PuvoWallpaperBase/src/main/java/com/puvo/livewallpapers/puvowallpaperbase/Visit.java)
object. Imagine you want a sprite in your wallpaper which will call a link to a specified URL, then you have to do the 
following

1. Add a special sprite with a name starting with *visit_* followed by a number to your resources 
(e.g. r3__3__visit_0__11__1__1__1__2538__823__0__2000.png)
2. In the customValues.xml file in your wallpaper module add the URL in the *visit_url* string array. The number you 
added to the sprite's name will be used as an index for this array. If you have a second URL in that list, then the 
*visit_1* sprite would be responsible for that. To enable/disable the sprites to open the URL yu can use the preferences,
which are defined in the prefs.xml file in the resource's xml directory.

### sending and receiving commands
The customValues.xml file has a *connections* string array. Every element represents a sprite and the values are
destination for commands of that sprite. *none* means that this sprite will not send any commands.
For example, if you tap one sprite on the screen it can send a *OPACITY_CHANGE* command with a value to another sprite.

## simulating a wallpaper
There is also a [Unity] (http://www.unity3d.com) project. You can use it to check your wallpaper before anny coding, to 
see if everything looks like expected. It's not perfect and currently it doesn't support animations, moving elements 
(like skyobjects, particles) and any kind of interaction. All you have to do is:

* open the "Wallpaper Creator" (Menu->Puvo->Wallpaper->Wallpaper Creator)
* add a directory to the \_sprites directory (in the Assets). Maybe the name of you wallpaper
* copy the sprites into that directory and change their "Pixel Per Unit" to 1 and Pivot to "Top Left"
* in the Wallpaper Creator enter that name in the Sprites Folder field
* enter the number of layers and their Offset Factors (the values for the *virtual_scroll_speed_factor* string array in 
the customValues.xml)
* press "Generate"

To test everything move the "Camera Position" slider to the left, press play and in play-mode move the slider.

## example
You can find the included TestWallpaper in google's play store under
[TestWallpaper] (https://play.google.com/store/apps/details?id=com.puvo.livewallpapers.testwallpaper)
