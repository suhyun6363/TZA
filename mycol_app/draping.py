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

    # print(personal_color)

    # 이미지 로드
    output_directory = os.path.join(settings.MEDIA_ROOT)
    output_filepath = os.path.join(output_directory, f'face_draping.png')

    face_draping_image = imageio.imread(output_filepath)
    face_draping_image = cv2.cvtColor(face_draping_image, cv2.COLOR_BGR2RGB)

    # 각 개인 색상에 대한 RGB 값 딕셔너리
    def select_rgb_values(personal_color):
        personal_color_rgb_values = {
            "Spring warm light": [
                (189, 129, 63),
                (248, 192, 168),
                (245, 138, 114),
                (248, 168, 94),
                (251, 239, 127),
                (199, 222, 122),
                (162, 219, 233),
                (149, 112, 177)
            ],
            "Spring warm bright": [
                (20, 15, 12),
                (246, 177, 161),
                (216, 49, 38),
                (224, 127, 41),
                (176, 163, 52),
                (43, 171, 80),
                (47, 85, 166),
                (47, 85, 166)
            ],
            "Summer cool light": [
                (68, 88, 140),
                (200, 143, 192),
                (238, 89, 146),
                (182, 183, 184),
                (101, 195, 181),
                (125, 162, 206),
                (120, 110, 177),
                (146, 114, 178)
            ],
            "Summer cool mute": [
                (137, 132, 144),
                (184, 136, 188),
                (198, 67, 116),
                (134, 128, 117),
                (65, 166, 137),
                (88, 116, 166),
                (106, 88, 165),
                (135, 97, 120)
            ],
            "Autumn warm mute": [
                (170, 125, 79),
                (242, 164, 129),
                (225, 106, 83),
                (226, 138, 62),
                (214, 203, 68),
                (129, 166, 63),
                (80, 156, 172),
                (119, 70, 153)
            ],
            "Autumn warm deep": [
                (20, 15, 12),
                (245, 150, 135),
                (116, 21, 20),
                (127, 74, 31),
                (77, 69, 26),
                (77, 105, 48),
                (20, 93, 120),
                (59, 32, 19)
            ],
            "Winter cool bright": [
                (20, 15, 11),
                (187, 84, 160),
                (237, 48, 139),
                (244, 134, 44),
                (244, 134, 44),
                (81, 83, 163),
                (108, 83, 162),
                (142, 83, 160)
            ],
            "Winter cool deep": [
                (62, 63, 103),
                (159, 51, 147),
                (169, 31, 93),
                (92, 37, 48),
                (15, 81, 41),
                (43, 41, 115),
                (64, 38, 107),
                (67, 30, 91)
            ]
        }
        return personal_color_rgb_values.get(personal_color, [])


    # 베스트
    selected_rgb_values_1 = select_rgb_values(personal_color)
    # 세컨드
    selected_rgb_values_2 = select_rgb_values(second_color)

    # 출력
    # print(f"Selected RGB Values for {personal_color}: {selected_rgb_values_1}")
    # print(f"Selected RGB Values for {second_color}: {selected_rgb_values_2}")

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

    # 두 번째 개인 색상에 대한 출력 및 이미지 생성 및 저장
    output_folder_2 = os.path.join(settings.MEDIA_ROOT, "second_draping")
    os.makedirs(output_folder_2, exist_ok=True)

    for i, rgb_values in enumerate(selected_rgb_values_2):
        output_image = face_draping_image.copy()
        black_pixels = np.all(output_image == [0, 0, 0], axis=-1)
        output_image[black_pixels] = [rgb_values[2], rgb_values[1], rgb_values[0]]
        output_path = os.path.join(output_folder_2, f"second_{i + 1}.png")
        cv2.imwrite(output_path, output_image)

    # 시각화
    fig, ax = plt.subplots(2, len(selected_rgb_values_1), figsize=(15, 6))

    # 첫 번째 개인 색상 시각화
    for i, rgb in enumerate(selected_rgb_values_1):
        color_patch = np.ones((100, 100, 3), dtype=np.uint8) * rgb
        ax[0, i].imshow(color_patch)
        ax[0, i].set_title(f"{personal_color} - {i + 1}")
        ax[0, i].axis("off")

    # 두 번째 개인 색상 시각화
    for i, rgb in enumerate(selected_rgb_values_2):
        color_patch = np.ones((100, 100, 3), dtype=np.uint8) * rgb
        ax[1, i].imshow(color_patch)
        ax[1, i].set_title(f"{second_color} - {i + 1}")
        ax[1, i].axis("off")

    #plt.show()
