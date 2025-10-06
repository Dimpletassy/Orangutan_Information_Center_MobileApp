import subprocess
import os

def run_gradle_debug():
    subprocess.run(["ls", "-l"]) 
    subprocess.run(["gradlew", "assembleDebug"], shell=True)
    subprocess.run(["gradlew", "installDebug"], shell=True)

def run_gradle():
    subprocess.run(["gradlew", "build"], shell=True)

def uninstall_app():
    subprocess.run(["gradlew", "uninstallAll"], shell=True)

def run_python_server():
    working_directory = os.path.join(os.path.dirname(__file__), 'backend')
    subprocess.run(["dir"], shell=True, cwd=working_directory)
    subprocess.run(["pip", "install", "flask"], shell=True, cwd=working_directory)
    subprocess.run(["python", "mockServer.py"], shell=True, cwd=working_directory)

    
if __name__ == "__main__":
    run_gradle_debug()
    run_python_server()