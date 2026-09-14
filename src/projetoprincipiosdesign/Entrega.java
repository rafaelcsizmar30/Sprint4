package projetoprincipiosdesign;

public interface Entrega {
    boolean isDisponivel(double total);
    double calcularFrete(double total);
}
