# # routing.py
# from channels.routing import ProtocolTypeRouter, URLRouter
# from channels.auth import AuthMiddlewareStack
# from django.urls import path
# from mycol_app.consumers import GlassesConsumer
#
# application = ProtocolTypeRouter(
#     {
#         "websocket": AuthMiddlewareStack(
#             URLRouter([path("ws/glasses/", GlassesConsumer.as_asgi())])
#         ),
#     }
# )
