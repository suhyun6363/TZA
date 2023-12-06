# mycol_app/views.py
from rest_framework.response import Response
#from rest_framework.decorators import api_view
from rest_framework.views import APIView
from rest_framework import status, generics
from .models import Analysis, UploadedImage
from .serializers import AnalysisSerializer, UploadedImageSerializer

from django.http import JsonResponse
from django.shortcuts import render, get_object_or_404
from rest_framework.parsers import FileUploadParser
from rest_framework.permissions import AllowAny

#퍼스널컬러진단 알고리즘을 호출하는 부분을 밑에 Class부분에 작성
#분석 결과를 얻고, 결과를 사용하여 PersonalColorDiagnosis객체를 업데이트함
#class PersonalColorDiagnosisAPIView(generics.CreateAPIView):

'''
class analysisApi(APIView):
    def post(self, request, *args, **kwargs):
        l_value = float(request.data.get('L'))
        b_value = float(request.data.get('b'))
        s_value = float(request.data.get('S'))

        analysis_data = {
            'l_value': l_value,
            'b_value': b_value,
            's_value': s_value,
        }

        serializer = analysisSerializer(data=analysis_data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
'''

class UploadImageView(APIView):
    def post(self, request, format=None):
        image_data = request.data.get('image')  # 'image'는 axios POST 요청에서 전송한 이미지 데이터의 키일 것입니다.

        # 받아온 이미지 데이터를 Serializer를 사용해 처리
        serializer = UploadedImageSerializer(data={'image': image_data})
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)

        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

class GetImageView(APIView):
    def get_image(request, image_id):
        # 이미지 모델에서 해당 ID에 해당하는 이미지 가져오기
        image = get_object_or_404(UploadedImage, pk=image_id)
        # 이미지 URL 가져오기 (이미지를 서빙하는 방식에 따라 조정해야 할 수 있음)
        image_url = request.build_absolute_uri(image.image.url)
        # JSON 응답으로 이미지 URL 반환
        return JsonResponse({'image_url': image_url})

class AnalysisList(APIView):
    def get(self, request):
        analyses = Analysis.objects.all()
        serializer = AnalysisSerializer(analyses, many=True)
        return Response(serializer.data)


'''
    def post(self, request):
        serializer = analysisSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
'''