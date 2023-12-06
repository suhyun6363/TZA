# mycol_app/serializers.py
from rest_framework import serializers
from .models import Analysis, UploadedImage

'''
class analysisSerializer(serializers.ModelSerializer):
    class Meta:
        model = diagnosis
        #fields = '__all__'
        fields = {'personal_color', 'second_color', 'personal_color_palette', 'second_color_palette'}
'''

class UploadedImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = UploadedImage
        fields = ('id', 'image','uploaded_at')

class AnalysisSerializer(serializers.ModelSerializer):
    class Meta:
        model = Analysis
        fields = '__all__'

