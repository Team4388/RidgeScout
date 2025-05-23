import os

ROOT = os.path.dirname(__file__)
METADATA_FILE = os.path.join(ROOT, 'metadata.json')
DATA_ROOT = os.path.join(os.path.dirname(__file__), 'data')

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
        with open(path) as f:
            return f.read()
    except:
        return None

def write(path, data):
    if not os.path.exists(path):
        with open(path, mode='a'): pass
    with open(path, 'w') as f:
        f.write(data)
