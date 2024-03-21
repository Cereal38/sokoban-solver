package Structures;

public class FAPTableau<E extends Comparable<E>> extends FAP<E> {
  SequenceTableau<E> s;

  public FAPTableau() {
    s = new SequenceTableau<>();
    super.s = s;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void insere(E element) {
    s.redimensionne();
    int courant = (s.debut + s.taille) % s.elements.length;
    int precedent = courant - 1;
    if (precedent < 0)
      precedent = s.elements.length - 1;
    while ((courant != s.debut) && (element.compareTo((E) s.elements[precedent]) < 0)) {
      s.elements[courant] = s.elements[precedent];
      courant = precedent;
      precedent = courant - 1;
      if (precedent < 0)
        precedent = s.elements.length - 1;
    }
    s.elements[courant] = element;
    s.taille++;
  }
}
