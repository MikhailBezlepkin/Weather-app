# Weather app
This app using the [Weather API](https://www.weatherapi.com/my/) and written in Kotlin with Android Studio.
***
## Weather app staсk:
* Using Volley for fetching data from Weather API;
* MVVM pattern;
* Picasso for image processing;
* Navigation-fragment.
***
## Description:
For the application to work correctly, you may need an individual key, which can be obtained by registering on the site [Weather API](https://www.weatherapi.com/my/).

After that, you need to insert it in place of the current key in the file com/example/weatherapp/const.kt

```kotlin
package com.example.weatherapp

val API_KEY = " c2a5bb1626ee4221b2d183827221310 "
```

* After launching the application, if geolocation is not enabled, a notification will be shown asking you to enable it.

![](https://github.com/MikhailBezlepkin/Screenshots/blob/main/WANot.png)

If you turn on geolocation, then data will be immediately displayed in accordance with your location, otherwise you will see a screen displaying weather data for the city of Omsk:

![](https://github.com/MikhailBezlepkin/Screenshots/blob/main/WAHo.png)

* In the lower right corner, a block displaying current data. There is a geolocation button, when clicked, data will be displayed according to your location.
On the left is the search button, by clicking on which you can see the weather by entering the name of the city or coordinates::

![](https://github.com/MikhailBezlepkin/Screenshots/blob/main/WAFi.png)

*When you select the "DAYS" section, you will see the weather forecast for the next three days. And you can see it in detail by selecting a specific day and go to the "HOURS" section

![](https://github.com/MikhailBezlepkin/Screenshots/blob/main/WADa.png)
