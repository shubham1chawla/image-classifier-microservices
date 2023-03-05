from requests import post
from os import listdir
from argparse import ArgumentParser
from time import time
from concurrent.futures import ThreadPoolExecutor

parser = ArgumentParser(description='Send requests for image classification')
parser.add_argument('--max', type=int, help='max number of requests to send to the server')
parser.add_argument('--api', type=str, help='API endpoint that will process this request')
parser.add_argument('--dir', type=str, help='directory containing dataset')
parser.add_argument('--multithread', action='store_true', help='send requests simultaneously')
args = parser.parse_args()

def post_request(path: str) -> None:
    try:
        response = post(args.api, files={'image': open(path,'rb')})
        if response.status_code != 200:
            print(f'sendErr: {response.url}')
            return
        print(f'{"-" * 60}\nResponse: {response.text}\n{"-" * 60}')
    except Exception as ex:
        print(ex)

def post_sequentially() -> None:
    for i, name in enumerate(listdir(args.dir)):
        if i == args.max:
            break
        post_request(args.dir + name)

def post_simultaneously() -> None:
    paths = []
    for i, name in enumerate(listdir(args.dir)):
        if i == args.max:
            break
        paths.append(args.dir + name)    
    with ThreadPoolExecutor(max_workers = min(args.max, 100)) as executor:
        executor.map(post_request, paths)

if __name__ == '__main__':
    start = time()
    if args.multithread:
        post_simultaneously()
    else:
        post_sequentially()
    print(f'\nTime taken (seconds): {time() - start}\n')