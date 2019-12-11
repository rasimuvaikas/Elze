import lemuoklio_kreipinys
lemos = [] # lemų (žodžių prie lemma)sąrašas, formuojamas skaitant iš failo
kiekiai = [] # skaitymo iš failo metu išrinkti lemų kiekiai
fv_duom = input("Įveskite failo pavadinimą ")
fv_rez = input("Įveskite rezultatų failo pavadinimą ")
print("Laukite rezultatų")
rez = open(fv_rez+".txt","w", encoding="utf-8")
failas = fv_duom+".txt"
lemos = ["vksm.", "dlv.", "bendr.", "pusd.", "pad."]
lemos2 = ["prv.", "prl.", "jng.", "dll.", "įv.", "nežinomas", "rom. sk.", "sutr.", "jst.", "išt."]

f = open(failas, encoding="utf-8")
tekst = f.read()
f.close()
teksteil = tekst.split("\n")
for tekst in teksteil:
    ats = lemuoklio_kreipinys.lemuoklis(tekst).split("\n")
    lemuotas = ""
    for eilute in ats: # skaitoma po eilutę
        if (eilute.find("lemma") > 0): # randama eilutė su žodžiu "lemma"
            eil = eilute.split('"') # suskaidoma į sąrašą
            kd = eil[5].split(",")
            if kd[0] not in lemos2 and kd[0] not in lemos: #atmetami jungtukai, prielinksniai, dalelytės, veiksmažodžiai
               lemuotas= lemuotas +" "+eil[3] # reikiamas žodis sąraše yra 4 (indeksas 3, nes indeksuojama nuo 0) įrašomas į lemų 
			   rez.write(lemuotas + "\n")
    
rez.close()
print("Rezultatai yra faile: "+fv_rez+".txt")

        
