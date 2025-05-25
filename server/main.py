from ast import mod
import os
import json
import hashlib
from datetime import datetime
from bottle import Bottle, run, get, put, static_file, response, request,HTTPResponse
from random import SystemRandom

from utils import *

app = Bottle()

file_metadata = {}

def save_metadata():
    global file_metadata
    write(METADATA_PATH4, json.dumps(file_metadata))

def load_metadata():
    global file_metadata
    data = read(METADATA_PATH4)
    if data is not None:
        file_metadata = json.loads(data)

api_key = None
cryptogen = SystemRandom()

def aquire_key():
    global api_key
    global cryptogen

    if api_key is None:
        try:
            api_key = read(API_KEY_PATH).decode("utf-8").strip()
        except:
            ran = cryptogen.randrange(10**80)
            api_key = "%064x" % ran
            write(API_KEY_PATH, api_key)

@app.route('/')
def list_html():
    global file_metadata
    load_metadata()

    content = '<html><body><table><tr>'
    for heading in ['File', 'Size', 'Modified', 'Sha256']:
        content += f'<th>{heading}</th>'
    content += "</tr>"

    print(file_metadata)

    for filename in file_metadata.keys():
        content += "<tr>"
        content += f'<td><a href="/api/{filename}">{filename}</a></td>'
        content += f'<td>{file_metadata[filename]["size"]}B</td>'
        content += f'<td>{file_metadata[filename]["modified"]}</td>'
        content += f'<td>{file_metadata[filename]["sha256"]}</td>'
        content += "</tr>"

    content += '</table></body></html>'

    return content


@app.route('/api/metadata')
def metadata():
    global file_metadata
    load_metadata()
    response.content_type = 'application/json'
    return json.dumps(file_metadata)


@app.route('/api/<filename>', method='PUT')
def upload(filename):
    global api_key
    try:
        sentkey = request.headers[API_KEY_HEADER]
        if sentkey != api_key:
            return HTTPResponse(status=403, body=f"Invalid Key {sentkey}, {api_key}")
    except:
        return HTTPResponse(status=403, body="You must specify an 'api_key' header")


    global file_metadata
    load_metadata()

    data = request.body.read()

    try:
        modified = request.headers[MODIFIED_HEADER]
    except:
        modified = (datetime.now() - datetime(1970, 1, 1)).total_seconds()

    sha256_hash = hashlib.sha256()
    sha256_hash.update(data)

    file_metadata[filename] = {
        'size': len(data),
        'modified': modified,
        'sha256': sha256_hash.hexdigest()
    }

    save_metadata()



    write(os.path.join(DATA_ROOT, filename), data)
    # save_metadata()
    # response.content_type = 'application/json'
    # return json.dumps(file_metadata)


@app.route('/api/<filename>')
def download(filename):

    data = read(os.path.join(DATA_ROOT, filename))

    if data is not None:
        response.content_type = 'application/octet-stream'
        return data
    else:
        HTTPResponse(status=404, body="File not found")

if __name__ == '__main__':
    mkdir(DATA_ROOT)
    aquire_key()
    app.run(host='localhost', port=8080)
