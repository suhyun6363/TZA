'''
#아직 미완성! 만드는중!

import qrcode
import requests

# 서버 URL과 id 설정
server_url = "http://127.0.0.1:8000/analysis/"
id_value = 1

# 서버에 요청하여 데이터 가져오기
response = requests.get(f"{server_url}{id_value}/")
analysis_data = response.json()

# QR 코드에 들어갈 데이터
qr_data = {
    "id": analysis_data["id"],
    "average_hsv": analysis_data["average_hsv"],
    "average_lab": analysis_data["average_lab"],
    "average_rgb": analysis_data["average_rgb"],
    "l_value": analysis_data["l_value"],
    "b_value": analysis_data["b_value"],
    "s_value": analysis_data["s_value"],
}

# 데이터를 JSON 문자열로 변환
json_string = json.dumps(qr_data)

# QR 코드 생성
qr = qrcode.QRCode(
    version=1,
    error_correction=qrcode.constants.ERROR_CORRECT_L,
    box_size=10,
    border=4,
)
qr.add_data(json_string)
qr.make(fit=True)

# 이미지 생성
img = qr.make_image(fill_color="black", back_color="white")

# 이미지 저장
img.save(f"qrcode_for_id_{id_value}.png")
'''
