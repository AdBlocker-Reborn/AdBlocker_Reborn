#!/usr/bin/env python
# -*- coding: utf-8; -*-
# # (c) free software, GPLv3
# Connect: oneleaf@gmail.com
# Modified by aviraxp

"""
Run：
   sudo python rules2regex.py > regexurl
"""

import urllib2


def addrules(url):
    html = urllib2.urlopen(url, timeout=10).readlines()
    for line in html:
        line = line.strip()
        if line == '': continue
        if line.find('$') >= 0: continue
        if line.find('#') >= 0: continue
        if line.find('@@') >= 0: continue
        if line.find('[]') >= 0: continue
        if line.startswith('!'): continue
        if line.startswith('['): continue
        line = line.replace('.', '\.')
        line = line.replace('^', '.')
        line = line.replace('|http', 'http')
        line = line.replace('||', '')
        line = line.replace('*', '.*')
        line = line.replace('?', '\?')
        line = line.replace('|', '')
        print line


if __name__ == '__main__':
    addrules("https://raw.githubusercontent.com/cjx82630/cjxlist/master/cjxlist.txt")
