import os

ROOT = os.path.dirname(__file__)
DATA_ROOT = os.path.join(os.path.dirname(__file__), 'server_data')

METADATA_PATH4 = os.path.join(ROOT, 'metadata.json')
API_KEY_PATH = os.path.join(ROOT, 'api_key.txt')

MODIFIED_HEADER = 'modified'
API_KEY_HEADER = 'api_key'

def mkdir(path):
    if not os.path.exists(path):
        os.makedirs(path)

def ls(path):
    try:
        return os.listdir(path)
    except:
        return []

def read(path):
    if not os.path.exists(path):
        return None
    try:
        with open(path, 'rb') as f:
            return f.read()
    except Exception as e:
        print(f"Error reading file {path}: {e}")
        return None

def write(path, data):
    if not os.path.exists(path):
        with open(path, mode='ab'): pass

    if isinstance(data, str):
        data = str.encode(data)

    with open(path, 'wb') as f:
        f.write(data)
