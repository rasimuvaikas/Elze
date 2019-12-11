import lemuoklio_kreipinys
import sys
lemos = [] # lemų (žodžių prie lemma)sąrašas, formuojamas skaitant iš failo
kiekiai = [] # skaitymo iš failo metu išrinkti lemų kiekiai
fv_duom = sys.argv[1]
fv_rez = sys.argv[2]
#print("Laukite rezultatų")
rez = open(fv_rez+".txt", "w", encoding="utf-8")
failas = fv_duom+".txt"
lemos = ["vksm.", "dlv.", "bendr.", "pusd.", "pad."]
f = open(failas, encoding="utf-8")
tekst = f.read()
f.close()
teksteil = tekst.split("\n")
for tekst in teksteil:
    ats = lemuoklio_kreipinys.lemuoklis(tekst).split("\n")
    #lemuotas = ""
    for eilute in ats: # skaitoma po eilutę
        lemuotas = ""
        if (eilute.find("lemma") > 0): # randama eilutė su žodžiu "lemma"
            eil = eilute.split('"') # suskaidoma į sąrašą
            kd = eil[5].split(",")
            #if kd[0] in lemos: #išskiriama veiksmažodžio bendratis (iš veiksmažodžių, dalyvių, pusdalyvių, padalyvių
              #  v = eil[3].split("(")
               # eil[3] = v[0]
            lemuotas= lemuotas +" "+ eil[1] + " " + eil[3] + " " + eil[5] # reikiamas žodis sąraše yra 4 (indeksas 3, nes indeksuojama nuo 0) įrašomas į lemų sąrašą
            rez.write(lemuotas + "\n")
            #print(lemuotas)

rez.close()

#print("Rezultatai yra faile: "+fv_rez+".txt")
