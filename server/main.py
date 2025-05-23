import os
import json
from bottle import Bottle, run, get, static_file, response

from utils import *

app = Bottle()

file_metadata = {}

def save_metadata():
    global file_metadata
    write(METADATA_FILE, json.dumps(file_metadata))

def load_metadata():
    global file_metadata
    data = read(METADATA_FILE)
    if data is not None:
        file_metadata = json.loads(data)

# @app.route('/')
# def list():
#     response.content_type = 'application/json'
#     return json.dumps(ls(DATA_ROOT))


@app.route('/api/metadata')
def metadata():
    global file_metadata
    load_metadata()
    response.content_type = 'application/json'
    return json.dumps(file_metadata)



# @app.route('/<filename>')
# def hello(filename):
#     return static_file(DATA_ROOT, filename)

if __name__ == '__main__':
    mkdir(DATA_ROOT)
    app.run(host='localhost', port=8080)
