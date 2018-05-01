import requests
from threading import Thread
import csv
import json
import sys
reload(sys)
sys.setdefaultencoding('utf8')
import csv, glob, os
import time
from Queue import Queue

def worker():
  while not q.empty():
        file = q.get()
        q.task_done()
        fileOut = csv.writer(open ("/home/user/Desktop/NHL Scraper/Player/player-"+ file[:-4] +".csv","wb+"))
        fileOut.writerow(["gameId","playerid","fullName","link","primaryNumber","birthDate","birthCity","birthStateProvince","birthCountry","nationality","height","weight","active","rookie","shootsCatches","rosterStatus","positionCode","positionName","positionType"])
        f = open(file)
        f.next()
        for line in f:
          array = line.split(",")
          req = "https://statsapi.web.nhl.com" + array[1]
          r = ''
          while r == '':
            try:
              r = requests.get(req)
            except:
              print("Connection refused by the server..")
              print("Let me sleep for 5 seconds")
              print("ZZzzzz...")
              time.sleep(5)
              print("Was a nice sleep, now let me continue...")
              continue

          if (r.status_code != 404):
            y = json.loads(str(r.content))
            for z in y["gameData"]["players"]:
              gameId = ''
              playerid = ''
              fullName = ''
              link = ''
              primaryNumber = ''
              birthDate = ''
              birthCity = ''
              birthStateProvince = ''
              birthCountry = ''
              nationality = ''
              height = ''
              weight = ''
              active = ''
              rookie = ''
              shootsCatches = ''
              rosterStatus = ''
              positionCode = ''
              positionName = ''
              positionType = ''
              x = y["gameData"]["players"][z]
              gameId = array[0]
              playerid = x["id"]
              fullName = x["fullName"]
              link = x["link"]
              if "primaryNumber" in x:
                primaryNumber = x["primaryNumber"]
              birthDate = x["birthDate"]
              if "birthCity" in x:
                birthCity = x["birthCity"]
              if "birthStateProvince" in x:
                birthStateProvince = x["birthStateProvince"]
              if "birthCountry" in x:
                birthCountry = x["birthCountry"]
              if "nationality" in x:
                nationality = x["nationality"]
              if "height" in x:
                height = x["height"]
              if "weight" in x:
                weight = x["weight"]
              if "active" in x:
                active = x["active"]
              if "rookie" in x:
                rookie = x["rookie"]
              if "shootsCatches" in x:
                shootsCatches = x["shootsCatches"]
              if "rosterStatus" in x:
                rosterStatus = x["rosterStatus"]
              if "primaryPosition" in x:
                positionCode = x["primaryPosition"]["code"]
                positionName = x["primaryPosition"]["name"]
                positionType = x["primaryPosition"]["type"]
              fileOut.writerow([gameId,playerid,fullName,link,primaryNumber,birthDate,birthCity,birthStateProvince,birthCountry,nationality,height,weight,active,rookie,shootsCatches,rosterStatus,positionCode,positionName,positionType])
        f.close()

os.chdir("/home/user/Desktop/NHL Scraper")
q = Queue()
for theFile in glob.glob("*.csv"):
  q.put(theFile)

for i in range(10):
     t = Thread(target=worker)
     t.daemon = True
     t.start()
q.join()

