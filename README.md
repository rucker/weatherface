Weatherface - a Pebble watchface that shows current weather conditions.
=================
PURPOSE
-----------------
The purpose of this project is essentially to satisfy my own curiosity while learning about the Pebble SDK and brushing up on Android. Also, it's a good excuse for me to get familiar with Git, Vim, and more.

DESIGN
----------------
Watchface will include a minimal time display -- probably a re-implementation of the standard "Text Watch" face that ships with the device --  as well as some information about the weather. The initial goal is to display the current temperature. Hopefully this will be expanded later on to include other information such as an icon to indicate the current conditions (sunny, cloudy, rainy, etc).

TODO/WISHLIST
---------------
1. Implement textual representation of time, e.g. "nine thirty nine" instead of "9:39."
	1a. Split up times ending in "teen" gracefully.
2. Set time correctly at init rather than when first tick is handled.
3. Implement communication with Android device.
4. Add weather icons.
