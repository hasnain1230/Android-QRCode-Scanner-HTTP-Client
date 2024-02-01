
# JSCJ QR Code Scanner Application
This is a QR code scanner application I built for Jafaria School. It is a customized QR code scanner that scans QR codes and functions based on the selected settings. QR codes can be scanned with in-real time QR code detection and image processing. Based on the selected settings, QR codes can simply be read, or if in **`HTTP` mode**, QR code data can be used in an HTTP request.

![icon.png](README%2Ficon.png)

# Author
- Hasnain Ali

# Libraries
This application uses the following libraries and APIs.
- AndroidX
- CameraX
- Google ML Kit
- OpenCV (For in-real time QR code detection)
- Camera2
- OkHttp
- ZXing

# How to Use The Application
The application will open to the following preview:
![Screenshot_20240201-110151.png](README%2FScreenshot_20240201-110151.png)

By default, the application will be in **`READ mode`** where QR codes will simply be read.
![Screenshot_20240201-111015.png](README%2FScreenshot_20240201-111015.png)

You can also navigate to settings to change to HTTP mode. There, you can configure your HTTP settings, and based on your settings, the application will send an HTTP request to your specified endpoint and settings.
![Screenshot_20240201-110912.png](README%2FScreenshot_20240201-110912.png)

# Playstore
Since this application is meant for JSCJ internal use at the moment, and since this application is in early alpha testing phase, this application is not available on the Playstore yet. However, within the new few weeks, I will be releasing it on the play store. If you have any requests for features, or any issues with the application, feel free to make a GitHub issues and I will address it as soon as I can. Thank you for understanding.
