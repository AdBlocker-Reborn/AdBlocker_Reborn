import urllib2


def addrules(url):
    html = urllib2.urlopen(url, timeout=10).readlines()
    for line in html:
        line = line.strip()
        if line == '': continue
        if line.find('|') >= 0: continue
        if line.find('~') >= 0: continue
        if line.find('$') >= 0: continue
        if line.find('@') >= 0: continue
        if line.find('third-party') >= 0: continue
        line = line.replace('*', '')
        print line


if __name__ == '__main__':
    addrules(
        "https://raw.githubusercontent.com/easylist/easylist/master/easylist/easylist_general_block.txt")