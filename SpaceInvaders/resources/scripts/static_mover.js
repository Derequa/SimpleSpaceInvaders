// Write a script that handles an event for a given static rectangle
// This script should be called by the event manager to handle a script event for a
// certain game object
function run(){
	//print('A script event has been handled');
	// Calculate new position
	newX = s.posGetX() + s.vMagX;
	// Check if we move past our bounds and change direction if we do
	if((newX > 390) || (newX < 30))
		s.vMagX *= -1;
	s.posSetX(newX);
}
