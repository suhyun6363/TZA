# mycol_app/serializers.py
from rest_framework import serializers
from .models import analysis

'''
class analysisSerializer(serializers.ModelSerializer):
    class Meta:
        model = diagnosis
        #fields = '__all__'
        fields = {'personal_color', 'second_color', 'personal_color_palette', 'second_color_palette'}
'''

class analysisSerializer(serializers.ModelSerializer):
    class Meta:
        model = analysis
        fields = '__all__'
