package Structures;

public abstract class FAP<E extends Comparable<E>> {
  Sequence<E> s;

  abstract void insere(E element);

  public E extrait() {
    return s.extraitTete();
  }

  public boolean estVide() {
    return s.estVide();
  }
}
