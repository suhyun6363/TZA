import os
import sys
import django

import numpy as np
import cv2
import imageio.v2 as imageio
import matplotlib.pyplot as plt

from config import settings

def run_draping():
    sys.path.append(os.path.join(os.path.dirname(__file__), 'config'))
    os.environ.setdefault("DJANGO_SETTINGS_MODULE", "config.settings")

    django.setup()
    from mycol_app.models import Analysis

    analysis_instance = Analysis.objects.latest('id')
    personal_color = analysis_instance.personal_color
    second_color = analysis_instance.second_color

    print(personal_color)

    # 이미지 로드
    output_directory = os.path.join(settings.MEDIA_ROOT)
    output_filepath = os.path.join(output_directory, f'face_draping.png')

    face_draping_image = imageio.imread(output_filepath)
    face_draping_image = cv2.cvtColor(face_draping_image, cv2.COLOR_BGR2RGB)
    print("1")

    # 각 개인 색상에 대한 RGB 값 딕셔너리
    def select_rgb_values(personal_color):
        personal_color_rgb_values = {
            "봄 Light": [
                (254, 212, 213),
                (255, 238, 160),
                (215, 247, 146),
                (164, 215, 234),
                (215, 171, 220)
            ],
            "봄 Bright": [
                (244, 64, 94),
                (255, 229, 1),
                (5, 172, 71),
                (4, 169, 207),
                (160, 41, 168)
            ],
            "여름 Light": [
                (243, 188, 211),
                (247, 223, 164),
                (145, 210, 185),
                (165, 193, 235),
                (180, 181, 229)
            ],
            "여름 Mute": [
                (202, 125, 151),
                (196, 200, 149),
                (154, 188, 162),
                (104, 113, 168),
                (136, 119, 161)
            ],
            "가을 Mute": [
                (229, 152, 135),
                (221, 202, 167),
                (185, 191, 111),
                (124, 153, 158),
                (156, 162, 184)
            ],
            "가을 Deep": [
                (149, 50, 42),
                (158, 110, 53),
                (80, 78, 44),
                (66, 73, 138),
                (59, 55, 79)
            ],
            "겨울 Bright": [
                (232, 60, 134),
                (253, 243, 107),
                (81, 157, 101),
                (71, 76, 140),
                (139, 51, 123)
            ],
            "겨울 Deep": [
                (159, 51, 66),
                (100, 56, 55),
                (46, 55, 34),
                (75, 69, 71),
                (76, 44, 106)

            ]
        }
        return personal_color_rgb_values.get(personal_color, [])


    # 베스트
    selected_rgb_values_1 = select_rgb_values(personal_color)
    # 세컨드
    selected_rgb_values_2 = select_rgb_values(second_color)

    # 출력
    print(f"Selected RGB Values for {personal_color}: {selected_rgb_values_1}")
    print(f"Selected RGB Values for {second_color}: {selected_rgb_values_2}")

    selected_rgb_values_1 = select_rgb_values(personal_color)
    selected_rgb_values_2 = select_rgb_values(second_color)

    # 첫 번째 개인 색상에 대한 출력 및 이미지 생성 및 저장
    output_folder_1 = os.path.join(settings.MEDIA_ROOT, "best_draping")
    os.makedirs(output_folder_1, exist_ok=True)

    for i, rgb_values in enumerate(selected_rgb_values_1):
        output_image = face_draping_image.copy()
        black_pixels = np.all(output_image == [0, 0, 0], axis=-1)
        output_image[black_pixels] = [rgb_values[2], rgb_values[1], rgb_values[0]]
        output_path = os.path.join(output_folder_1, f"best_{i + 1}.png")
        cv2.imwrite(output_path, output_image)
    print("best 생성완료")

    # 두 번째 개인 색상에 대한 출력 및 이미지 생성 및 저장
    output_folder_2 = os.path.join(settings.MEDIA_ROOT, "second_draping")
    os.makedirs(output_folder_2, exist_ok=True)

    for i, rgb_values in enumerate(selected_rgb_values_2):
        output_image = face_draping_image.copy()
        black_pixels = np.all(output_image == [0, 0, 0], axis=-1)
        output_image[black_pixels] = [rgb_values[2], rgb_values[1], rgb_values[0]]
        output_path = os.path.join(output_folder_2, f"second_{i + 1}.png")
        cv2.imwrite(output_path, output_image)
    print("second 생성완료")


    print("2")


    # 시각화
    #fig, ax = plt.subplots(2, len(selected_rgb_values_1), figsize=(15, 6))

    # 첫 번째 개인 색상 시각화
    # for i, rgb in enumerate(selected_rgb_values_1):
    #     color_patch = np.ones((100, 100, 3), dtype=np.uint8) * rgb
    #     ax[0, i].imshow(color_patch)
    #     ax[0, i].set_title(f"{personal_color} - {i + 1}")
    #     ax[0, i].axis("off")
    #
    # # 두 번째 개인 색상 시각화
    # for i, rgb in enumerate(selected_rgb_values_2):
    #     color_patch = np.ones((100, 100, 3), dtype=np.uint8) * rgb
    #     ax[1, i].imshow(color_patch)
    #     ax[1, i].set_title(f"{second_color} - {i + 1}")
    #     ax[1, i].axis("off")

    #plt.show()
