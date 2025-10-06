from flask import Flask
from flask import request

app = Flask(__name__)

BASE_API = "/api/v1"

@app.route(f"{BASE_API}/", methods=["GET"])
def test():
    request_data = request.query_string.decode('utf-8')
    param_dict = []
    for param in request_data.split('&'):
        key, value = param.split('=')
        param_dict.append({key: value})
    return {
        "status": "success",
        "data": param_dict
    }



if __name__ == "__main__":
    app.run(host='192.168.1.133', port=3000)