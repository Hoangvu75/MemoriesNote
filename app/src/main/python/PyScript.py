import os
from os.path import join, dirname

# create function to read file "UniqueCode.txt", then return the code in the file, if the file is empty, generate the code then write to the file
def read_code():
    filename = join(os.environ["HOME"], "UniqueCode.txt")
    if os.path.exists(filename):
        with open(filename, "r") as f:
            code = f.read()
    else:
        code = generate_code()
        with open(filename, "w") as f:
            f.write(code)
    return code

def generate_code():
    import random
    code = "user"
    for i in range(10):
        code += str(random.randint(0, 9))
    return code

