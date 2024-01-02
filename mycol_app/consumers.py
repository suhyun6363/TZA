# # consumers.py
# import json
# from channels.generic.websocket import AsyncWebsocketConsumer
#
# class GlassesConsumer(AsyncWebsocketConsumer):
#     async def connect(self):
#         await self.accept()
#
#     async def disconnect(self, close_code):
#         pass
#
#     async def receive(self, text_data):
#         text_data_json = json.loads(text_data)
#         eyeglass_status = text_data_json["eyeglass_status"]
#
#         await self.send(text_data=json.dumps({"eyeglass_status": eyeglass_status}))
