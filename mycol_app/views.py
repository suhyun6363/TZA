# mycol_app/views.py
#from rest_framework import generics
#from .models import PersonalColorDiagnosis
#from .serializers import PersonalColorDiagnosisSerializer
from rest_framework.response import Response
#from rest_framework.decorators import api_view
from rest_framework.views import APIView
from rest_framework import status
from .models import analysis
from .serializers import analysisSerializer

#퍼스널컬러진단 알고리즘을 호출하는 부분을 밑에 Class부분에 작성
#분석 결과를 얻고, 결과를 사용하여 PersonalColorDiagnosis객체를 업데이트함
#class PersonalColorDiagnosisAPIView(generics.CreateAPIView):

'''
#test
@api_view(['GET'])
def helloApi(request):
    return Response("hello world!")
'''
'''
#test2
@api_view(['POST'])
def analysisApi(request):
    totalDiagnosis = analysis.objects.all()
    serializer = analysisSerializer(totalAnalysis, many=True)
    return Response(serializer.data)
'''

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

class AnalysisList(APIView):
    def get(self, request):
        analyses = analysis.objects.all()
        serializer = analysisSerializer(analyses, many=True)
        return Response(serializer.data)

    def post(self, request):
        serializer = analysisSerializer(data=request.data)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)