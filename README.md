# TZA - 톤잘알
### : 당신에게 어울리는 톤을 잘 알려드립니다 </br></br>
![tza로고](https://github.com/suhyun6363/TZA/assets/90364745/420c2daf-eeb4-4880-a572-54ab08a554c9.png)
</br>

## 목차   
___
- [TZA 소개](#TZA-소개)
  - [주요 기능](#주요-기능)
  - [개발 환경](#개발-환경)
  - [개발 기간](#개발-기간)
- [구현 결과](#구현-결과)
- [팀원](#팀원)


## TZA 소개
___  
- K-뷰티의 열풍과 함께 퍼스널 컬러에 대한 관심이 국내뿐만 아니라 해외
에서도 확산되고 있음  
- 제작한 진단 부스에서 촬영한 안면 이미지를 이용하여 정확하고 간편한 퍼스널 컬러 진단 & 애플리케이션을 통한 개인 맞춤형 뷰티 서비스</br>   

|              **제작한 진단 부스**               |                **안드로이드 애플리케이션**                |
|:----------------------------------------:|:----------------------------------------------:|
| <img src="https://github.com/suhyun6363/TZA/assets/90364745/18b720c4-79f2-4427-bb9d-46e7151ec484.png" width=250> | <img src="https://github.com/suhyun6363/TZA/assets/90364745/a10bb862-95e5-46df-9561-9240937ac246.png" width=250> |
### 주요 기능
#### 웹
+ 피부 영역 픽셀 기반 퍼스널 컬러 진단
   + [Mediapipe의 face landmarker](https://ai.google.dev/edge/mediapipe/solutions/vision/face_landmarker)을 이용하여 안면의 피부 영역을 제외한 모든 부위 제거
   + 피부 영역 이미지 픽셀을 K-means clustering, `K=8`
   + 군집화 영역 K 중 `픽셀 수가 가장 많은 클러스터`(Most Pixels), 이 클러스터를 기준으로 `color distance가 짧은 클러스터 2개`(1st Smallest Color Distance, 2nd Smallest Color Distance)를 구해 3개의 클러스터의 가중 평균을 통한 `대표 피부색`(Total Weighted Mean Color) 추출   
   <br>
        <img src="https://github.com/suhyun6363/TZA/assets/90364745/610b255e-5b62-4873-9b24-e803cf691fbc.png" width=250>   
  
   + [퍼스널 컬러 스킨 톤 유형 분류의 정량적 평가 모델 구축에 대한 연구](https://dx.doi.org/10.5850/JKSCT.2018.42.1.121) 논문의 기준값(`(V0, b*0, S0)=(65.20, 18.50, 33)`)을 참고
   + 위 논문은 측색기를 이용한 측색값 표본으로 구한 기준값이므로 [20대~30대 한국 여성의 베스트컬러에 관한연구](https://www.doi.org/10.23174/hongik.000000000376.11064.0000164) 논문을 참고하여 대표 피부색에서 오차 `V값 +12`, `S값 –4` 피부값 보정
   + 기준값을 기준, 대표 피부색 Lab 색공간의 b(황색도)값으로 `웜톤/쿨톤`을 구분, 대표 피부색 HSV 색공간의 V(명도)와 S(채도)로 계절별 세부 톤을 구분하여 `봄 웜 라이트/봄 웜 브라이트/여름 쿨 라이트/여름 쿨 뮤트/가을 웜 뮤트/가을 웜 딥/겨울 쿨 브라이트/겨울 쿨 딥`을 진단</br>
   <br>
        <img src="https://github.com/suhyun6363/TZA/assets/90364745/a603c0e9-9363-476a-ac94-2dd3c6c656ee.png" width=250>


   + 대표 피부색 Lab 색공간의 b가 `17 <= b* <20`인 경우, `뉴트럴 톤` 진단 </br>
   + 일련의 과정은 [Web_BE/mycol_app/facemesh.py](https://github.com/suhyun6363/TZA/blob/fda24433b90075dcde63d8c8b3034faddb437635/Web_BE/mycol_app/facemesh.py)에서 코드로 확인 가능
   <br>
#### 안드로이드 애플리케이션
+ 색조 메이크업 제품 정보 제공 서비스
    + 필터링
      + 올리브영 제품 크롤링 &rarr; 크롤링한 제품 컬러칩 리스트를 위와 동일한 방식으로 군집화하여 `각 컬러칩의 대표 색상`을 산출
      + 립/아이/블러셔 카테고리당 무게중심을 구함 &rarr; 구한 무게중심을 기준값으로 하여 위와 동일한 방식으로 `컬러칩 대표 색상 퍼스널 컬러` 분류
        + 립 카테고리 무게중심: (V0, b*0, S0)=(78, 12, 54)
        + 아이 카테고리 무게중심: (V0, b*0, S0)=(81, 7, 21)
        + 블러셔 카테고리 무게중심: (V0, b*0, S0)=(87, 12, 34)
      + 리뷰 데이터를 활용해 수분감 분류
    + 진단된 퍼스널 컬러에 해당하는 메이크업 제품 정보 제공
      ```
      .whereEqualTo("category_list2", category)
      .whereGreaterThanOrEqualTo("average_rate", 4.7)     // 카테고리별 색조 제품 상위 30%의 후기가 좋은 제품
      .whereEqualTo("result", result)
      .whereEqualTo("moisturizing", 1 | 0)       // 수분감: 촉촉 | 매트
      ```
       [Android/app/src/main/java/kr/ac/duksung/mycol/RecommendFragment.java](https://github.com/suhyun6363/TZA/blob/fda24433b90075dcde63d8c8b3034faddb437635/Android/app/src/main/java/kr/ac/duksung/mycol/RecommendFragment.java)에서 확인 가능
    + 모든 퍼스널 컬러 유형의 추천 제품을 보여주는 종합 랭킹 페이지 [Android/app/src/main/java/kr/ac/duksung/mycol/TotalRecommendFragment.java](https://github.com/suhyun6363/TZA/blob/fda24433b90075dcde63d8c8b3034faddb437635/Android/app/src/main/java/kr/ac/duksung/mycol/TotalRecommendFragment.java)에서 확인 가능 </br>
   <br>
+ 가상 메이크업
  +  이미지를 촬영한 후, `내장된 Mediapipe의 face landmarker 모델`로 얼굴 검출 및 얼굴 landmark 인식
  + 얼굴 landmark 결과값과 OpenCV를 이용해 블러셔와 립 메이크업 필터 연산, 생성 
  + 해당 이미지 위에 추천 제품을 가상으로 체험
### 개발 환경
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Python](https://img.shields.io/badge/python-3670A0?style=for-the-badge&logo=python&logoColor=ffdd54)
![Mediapipe](https://img.shields.io/badge/mediapipe-007986?style=for-the-badge&logo=mediaipe&logoColor=black) 
![OpenCV](https://img.shields.io/badge/OpenCV-27338e?style=for-the-badge&logo=OpenCV&logoColor=white) 
![Selenium](https://img.shields.io/badge/-selenium-%43B02A?style=for-the-badge&logo=selenium&logoColor=white) </br>
![React](https://img.shields.io/badge/React-282C34?style=for-the-badge&logo=react&logoColor=61DAFB)
![Android Studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android&logoColor=white) </br>
![Django](https://img.shields.io/badge/Django-092E20?style=for-the-badge&logo=django&logoColor=green)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-ffca28?style=for-the-badge&logo=firebase&logoColor=black) </br>
![Amazon AWS](https://img.shields.io/badge/Amazon_AWS-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white) </br>
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white) </br>
![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)

### 개발 기간
` 2023.11.04 ` - ` 2024.07.~`

## 구현 결과
___
### 웹
|                 **시작 화면**                  |             **퍼스널 컬러 진단 결과 화면**              |           **해당 퍼스널 컬러의 대표 연예인 화면**            |                               **QR코드 화면**                                |
|:------------------------------------------:|:--------------------------------------------:|:---------------------------------------------:|:------------------------------------------------------------------------:|
| <img src="https://github.com/suhyun6363/TZA/assets/90364745/e1bb4a27-f5e9-46e8-8880-a91fbd5d52e2.png" width=300> | <img src="https://github.com/suhyun6363/TZA/assets/90364745/e58577fb-6da2-4c9f-afce-05c32505ac80.png" width=300> | <img src="https://github.com/suhyun6363/TZA/assets/90364745/e654782d-91fc-4aa5-9899-85b02b88e30c.png" width=300> | <img src="https://github.com/suhyun6363/TZA/assets/90364745/af8a0046-3f5d-4c49-a250-b2dd67feee48.png" width=300> <br/> QR코드를 통해 앱으로 진단 결과 전송 |        

### 안드로이드 애플리케이션
|                 **런처 화면**                  |            **회원가입 화면**            |                                                    **로그인 화면**                                                    |
|:--------------------------------------------:|:--------------------------------------------:|:----------------------------------------------------------------------------------------------------------------:|
| <img src="https://github.com/suhyun6363/TZA/assets/90364745/ab4aba58-d932-4673-a208-f6cd8be73291.png" width=200> | <img src="https://github.com/suhyun6363/TZA/assets/90364745/538b0b46-239c-4e34-b47c-f0a497e26f45.png" width=200> | <img src="https://github.com/suhyun6363/TZA/assets/90364745/e97300da-5b9c-463b-a2a9-b10cffce1999.png" width=200> |

|                 **메인/홈 화면**                  |            **퍼스널 컬러 맞춤 추천 제품 화면**            |                **종합 랭킹 화면**                 |
|:--------------------------------------------:|:--------------------------------------------:|:-------------------------------------------:|
| <img src="https://github.com/suhyun6363/TZA/assets/90364745/ca5489de-2abb-45f9-aca3-e14b5dc7f222.gif" width=200> | <img src="https://github.com/suhyun6363/TZA/assets/90364745/b6de3680-f422-4e75-bc2e-bdc7303ba2bb.gif" width=200> | <img src="https://github.com/suhyun6363/TZA/assets/90364745/c976d408-4c33-4636-8ade-c8a917766765.gif" width=200> |

|                                             **블러셔/립 컬러칩 대표 색상 리스트**                                              |            **가상 메이크업 화면**            |                **마이페이지 화면**                 |
|:----------------------------------------------------------------------------------------------------------------:|:--------------------------------------------:|:-------------------------------------------:|
| <img src="https://github.com/suhyun6363/TZA/assets/90364745/d51435d7-abd8-4ace-9ff7-b9388fcb624d.gif" width=200><br><img src="https://github.com/suhyun6363/TZA/assets/90364745/6a5db60a-bdea-442e-aaf6-cf431a9b436e.gif" width=200> | <img src="https://github.com/suhyun6363/TZA/assets/90364745/3bb8c3a7-c1a9-4023-92a4-b2f6c1fb5d22.png" width=300> | <img src="https://github.com/suhyun6363/TZA/assets/90364745/8afb3983-ba83-46d3-9104-51c967f03d6a.png" width=200> |


## 팀원
___
|**안예은**|**이규빈**|                                                                                                                          **정수현**                                                                                                                          | **김희수** |
|:-----:|:-----:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:-------:|
|       |       | [<img src="https://github.com/suhyun6363/TZA/assets/90364745/657d2e8f-3697-43e1-a729-3336f1523a39.png" width=100> <br/> <img src="https://upload.wikimedia.org/wikipedia/commons/9/91/Octicons-mark-github.svg" width=25>](https://github.com/suhyun6363) |         |



