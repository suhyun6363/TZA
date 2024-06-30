# mycol_app/views.py
from rest_framework.response import Response
from rest_framework.views import APIView
from rest_framework import status, generics
from .models import Analysis, UploadedImage
from .serializers import AnalysisSerializer, UploadedImageSerializer

from django.http import JsonResponse
from django.shortcuts import render, get_object_or_404

class UploadImageView(APIView):
    def post(self, request, format=None):
        image = request.FILES.get('image')  # 이미지 파일 받아오기
        if image:
            # 받아온 이미지를 UploadedImage 모델에 저장
            uploaded_image = UploadedImage(image=image)
            uploaded_image.save()

            # 저장된 이미지 정보를 시리얼라이저를 통해 JSON 형태로 반환
            serializer = UploadedImageSerializer(uploaded_image)
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        else:
            return Response({"error": "Image upload failed."}, status=status.HTTP_400_BAD_REQUEST)

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

# class GetColorView(APIView):
class GetLatestAnalysisView(APIView):
    def get(self, request, format=None):
        # Analysis 모델의 가장 최근 객체를 가져옵니다.
        latest_analysis = Analysis.objects.latest('id')

        # Analysis 객체의 모든 값을 시리얼라이저를 통해 JSON 형태로 응답합니다.
        serializer = AnalysisSerializer(latest_analysis)
        return Response(serializer.data)
