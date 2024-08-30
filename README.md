
# TB Test App

A mobile application designed for the automated analysis of the Mantoux Tuberculin Skin Test (TST) to aid in the diagnosis of latent tuberculosis infection (LTBI). This Android application integrates image processing and machine learning techniques to enhance the accuracy and accessibility of TST analysis.

## Features

- **Automated Image Analysis**: Utilizes deep learning models for the segmentation and analysis of skin induration images from the TST.
- **User-Friendly Interface**: Simplifies the process of capturing and analyzing skin test results through an intuitive UI.
- **Integration with Machine Learning**: Employs models like YOLO, SAM, and DeepLabV3 for accurate image segmentation and analysis.
- **Enhanced Diagnostics**: Provides consistent, reliable measurements of skin indurations, reducing the variability and subjectivity of manual assessments.

## Project Objectives

1. **Image Processing**: Develop algorithms capable of identifying and measuring swelling areas in the Mantoux test images.
2. **Machine Learning Integration**: Implement a model for diagnosing LTBI based on processed images.
3. **Mobile Deployment**: Integrate the algorithms into a functional Android application for widespread use.

## Installation

1. Clone the repository:
   ```
   git clone https://github.com/lionggele/TB_test_app.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build the project.
4. Run the app on an Android device or emulator.

## How It Works

1. **Image Capture**: Users capture an image of the Mantoux test site using their smartphone camera.
2. **Image Segmentation**: The app processes the image using the integrated machine learning models to segment and identify the area of swelling.
3. **Analysis and Results**: The segmented images are analyzed to provide a measurement of induration, which is then used to assess the presence of LTBI.

## Future Work

- **Expand Dataset**: Incorporate a broader range of real-world data to improve model accuracy.
- **Enhance AR Features**: Integrate ARCore for better depth measurement and visualization.
- **Clinical Testing**: Conduct thorough testing in clinical settings to validate the app’s accuracy and reliability.

## Contributing

Contributions are welcome! Please open an issue or submit a pull request for any improvements or bug fixes.

## License

This project is licensed under the MIT License.
