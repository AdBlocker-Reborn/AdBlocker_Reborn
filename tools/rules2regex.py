#!/usr/bin/env python
# -*- coding: utf-8; -*-
# # (c) free software, GPLv3
# Connect: oneleaf@gmail.com
# Modified by aviraxp

"""
SQUID Config：
   acl adblock url_regex "/etc/squid/adblock.acl"
   http_access deny adblock

Run：
   sudo python rules2regex.py > /etc/squid/adblock.acl
"""

import urllib2


def addrules(url):
    html = urllib2.urlopen(url, timeout=60).readlines()
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
