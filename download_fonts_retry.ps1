$baseUrl = 'https://github.com/google/fonts/raw/main/ofl'
$fontDir = 'c:\Users\thewo\OneDrive\Desktop\Projeler\telsiz-okulu-android\app\src\main\res\font'

Invoke-WebRequest -Uri "$baseUrl/inter/static/Inter-Regular.ttf" -OutFile "$fontDir\inter_regular.ttf"
Invoke-WebRequest -Uri "$baseUrl/inter/static/Inter-SemiBold.ttf" -OutFile "$fontDir\inter_semibold.ttf"
Invoke-WebRequest -Uri "$baseUrl/inter/static/Inter-Bold.ttf" -OutFile "$fontDir\inter_bold.ttf"
Invoke-WebRequest -Uri "$baseUrl/inter/static/Inter-ExtraBold.ttf" -OutFile "$fontDir\inter_extrabold.ttf"

echo 'Fonts downloaded!'
