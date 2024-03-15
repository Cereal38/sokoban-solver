package Vue;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class AdaptateurClavier extends KeyAdapter {
  CollecteurEvenements control;

  AdaptateurClavier(CollecteurEvenements c) {
    control = c;
  }

  @Override
  public void keyPressed(KeyEvent event) {
    switch (event.getKeyCode()) {
      case KeyEvent.VK_LEFT:
        control.toucheClavier("Left");
        break;
      case KeyEvent.VK_RIGHT:
        control.toucheClavier("Right");
        break;
      case KeyEvent.VK_UP:
        control.toucheClavier("Up");
        break;
      case KeyEvent.VK_DOWN:
        control.toucheClavier("Down");
        break;
      case KeyEvent.VK_Q:
      case KeyEvent.VK_A:
        control.toucheClavier("Quit");
        break;
      case KeyEvent.VK_P:
        control.toucheClavier("Pause");
        break;
      case KeyEvent.VK_I:
        control.toucheClavier("IA");
        break;
      case KeyEvent.VK_ESCAPE:
        control.toucheClavier("Full");
        break;
    }
  }
}
