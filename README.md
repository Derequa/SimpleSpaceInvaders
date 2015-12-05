This is the first game I have made on my 2D game engine. Its currently a simple repeating level of space invaders.
It is designed ot be a multiplayer game, but you can still get a single player expierence by playing directly on the game server.


TO BUILD: This is essentailly an eclipse project, you should be able to clone it in directly, but if not, it should be quite easy to import as an archive file.


BUGS: This game is in a quasi-finisihed state. It was up to the spec required for a homework, but I am currently in the process of expanding and further fixing it. I just update its version of processing to 3.0, and all the artwork is currently place-holder. I also have found bugs with regards to player death-events and randomly getting collided on invisible objects. These are not always obvious but I am actively aware and will push fixes as soon as I can.


To run the server side, YOU MUST RUN THE SERVER RUNNER CLASS because that way java can find the resource files it needs.
Same deal with the client side RUN THE CLIENT RUNNER CLASS.


Controls:

W - move up / tap to jump

A - move left

Space = shoot missile
    
Playing the game on a client allows you to use the replay system.


Replays:
    When the Client class is run, a GUI will appear that will have controls for recording a replay.
    You can record a replay by clicking "Start Recording" and you can stop recording by clicking "Stop Recording".
    When you stop recording your first replay, a new PApplet window will appear to view your replays on whileyou continue to play.
    When you have a replay recorded, you can play it by clicking "Play" and pause it by clicking "Pause".
    When a replay is playing you can change the playback speed by clicking on one of the speed buttons.
    The "1/2 Speed" button slows down the repay to half speed.
    The "2x Speed" button speeds up the repay to double speed.
    The "Default Speed" button sets the repay to realtime speed.
    Once the replay has finished playing, the viewer will go blank. The replay can be restarted with the restart button.
    The replay will then start playing at the speed (if any) it was when it finished.
    If you wish to record another replay, simply click "Start Recording" again, but watch out, this will overwrite your last replay.
    Be warned: if you close the client, replay GUI or replay viewer all the open windows associated with this client will close.
