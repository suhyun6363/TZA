# mycol_app/serializers.py
from rest_framework import serializers
from .models import Analysis, UploadedImage, WearingDetect

class UploadedImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = UploadedImage
        fields = ('id', 'image','uploaded_at')

class AnalysisSerializer(serializers.ModelSerializer):
    class Meta:
        model = Analysis
        fields = '__all__'

class WearingDetectSerializer(serializers.ModelSerializer):
    class Meta:
        model = WearingDetect
        fields = '__all__'

