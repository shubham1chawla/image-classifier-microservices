#!/usr/bin/python3

from sys import argv, exit
from csv import reader
from os import sep, path
from logging import error

results = dict()
with open(path.join(path.dirname(path.realpath(__file__)), 'results.csv')) as csv:
    rows = reader(csv)
    for row in rows:
        results[row[0]] = row[1]

if __name__ == '__main__':
    try:
        if len(argv) == 1 or len(argv[1]) == 0:
            raise Exception('Please provide a valid image!')
        key = argv[1].split(sep).pop()
        result = results[key] if results.get(key) != None else 'unknown'
        print('{ "key": "%s", "result": "%s" }' % (key, result))
    except Exception as ex:
        error(ex)
        exit(1)