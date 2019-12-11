import urllib.parse
import urllib.request

def lemuoklis(tekst): #lema + morfologija
    url = 'http://donelaitis.vdu.lt/NLP/nlp.php' # VDU KLC lemuoklio servisas
   # tekst -  pateikiamas tekstas lemavimui

    values = {'tekstas' : tekst,
              'tipas': 'anotuoti',
              'pateikti': 'LM',
              'veiksmas': 'Analizuoti'}

    data = urllib.parse.urlencode(values)
    data = data.encode('utf-8')    # paverciam duomenis i bytes formata
    req = urllib.request.Request(url, data)
    response = urllib.request.urlopen(req)
    lemrez = response.read()
    lemrez_str = str(lemrez, encoding='UTF-8') # paverciam duomenis i string formata
    #print (lemrez_str)
    return lemrez_str

def lemuoklis2(tekst): # tik lema
    url = 'http://donelaitis.vdu.lt/NLP/nlp.php' # VDU KLC lemuoklio servisas
    # tekst -  pateikiamas tekstas lemavimui
    values = {'tekstas' : tekst,
              'tipas': 'anotuoti',
              'pateikti': 'L',
              'veiksmas': 'Analizuoti'}

    data = urllib.parse.urlencode(values)
    data = data.encode('utf-8')    # paverciam duomenis i bytes formata
    req = urllib.request.Request(url, data)
    response = urllib.request.urlopen(req)
    lemrez = response.read()
    lemrez_str = str(lemrez, encoding='UTF-8') # paverciam duomenis i string formata
    #print (lemrez_str)
    return lemrez_str
