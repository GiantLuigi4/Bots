from com.tfc.openAI.lang.utils import InputList
from java.util import Random
rand = Random()
getPixel = %display%
aiInstance = %id%
def keyPress(key):
    InputList.add(aiInstance, 'key:'+str(key))
def click(key):
    InputList.add(aiInstance, 'click:'+str(key))
def mouseMove(key):
    InputList.add(aiInstance, 'mouseMove:'+str(key))
def AI():
    space = 32
    enter = 10
    words = []
    word = ''
    for array in getPixel:
        for letter in array:
            print(chr(letter))
            if letter == space:
                words.append(word)
                word = ''
            elif letter!=enter:
                word = word+str(chr(letter))
    words.append(word)
    print(words)
AI()
