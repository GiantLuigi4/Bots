from com.tfc.openAI.lang.utils import InputList
from java.util import Random

rand = Random()
getPixel = % display %
aiInstance = % id %


def keyPress(key):
    InputList.add(aiInstance, 'key:' + str(key))


def click(key):
    InputList.add(aiInstance, 'click:' + str(key))


def mouseMove(key):
    InputList.add(aiInstance, 'mouseMove:' + str(key))


def AI():
    from utils import Files
    from utils import PropertyReader
    from java.io import File
    from java.lang import Boolean
    space = 32
    enter = 10
    words = []
    word = ''
    input = ''
    for letter in getPixel[0]:
        if letter == space:
            words.append(word)
            input = input + word + ' '
            word = ''
        elif letter != enter:
            word = word + str(chr(letter))
    words.append(word)
    print(words)
    for f in Files.listAllFolders(Files.get("bots\\convo\\simple")):
        inputs = File(f.getPath() + "\\in.list")
        outputs = File(f.getPath() + "\\out.list")
        info = File(f.getPath() + "\\info.properties")
        inputsArray = Files.readArray(inputs)
        caseSensitive = Boolean.parseBoolean(PropertyReader.read(info, "caseSensitive"))
        ends = PropertyReader.read(info, "validEnds")
        check = input.lower()
        if caseSensitive:
            check = input
        msg = ""
        for inp in inputsArray:
            if ends == "":
                if check.startswith(inp):
                    rng = Random()
                    out = Files.readArray(outputs)
                    msg = out[rng.nextInt(out.length)]
                    break
            else:
                if check.startswith(inp):
                    rng = Random()
                    out = Files.readArray(outputs)
                    msg = out[rng.nextInt(out.length)]
                    break
                for c in ends:
                    if check == (inp + c):
                        rng = Random()
                        out = Files.readArray(outputs)
                        msg = out[rng.nextInt(out.length)]
                        break
            if not msg == "":
                break
        if msg != 'null' and not msg == "":
            keyPress(msg)


AI()
