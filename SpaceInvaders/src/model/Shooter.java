package model;

import java.awt.Rectangle;

import events.MissleEvent;
import server.GameManager;

public class Shooter {
	
	GameManager parent;
	public Shooter(GameManager parent){
		this.parent = parent;
	}

	public void handleShot(MissleEvent e) {
		GameObject g = GameManager.objects.get(new Integer(e.creatorGUID));
		if(g instanceof Player){
			if(((Player) g).cooldown > 0)
				return;
			// Make new missle at the player's location and fire up
			Missle m = new Missle(GameManager.guidMaker++, new Rectangle(g.posGetX(), g.posGetY(), Missle.MISSLE_SIZE / 2, Missle.MISSLE_SIZE), parent, g);
			m.vY = -Missle.MISSLE_V;
			m.setSprite(GameManager.imageLib.get(4));
			m.setVisible(true);
			m.playerFired = true;
			m.movement = GameManager.motionUpdater;
			GameManager.objects.put(m.getGUID(), m);
			((Player) g).cooldown = 10;
		}
		else if(g instanceof Box){
			// Make new missle at the box and fire down
			Missle m = new Missle(GameManager.guidMaker++, new Rectangle(g.posGetX(), g.posGetY(), Missle.MISSLE_SIZE / 2, Missle.MISSLE_SIZE), parent, g);
			m.vY = Missle.MISSLE_V;
			m.setSprite(GameManager.imageLib.get(3));
			m.setVisible(true);
			m.movement = GameManager.motionUpdater;
			GameManager.objects.put(m.getGUID(), m);
		}
		else return;
	}
}
