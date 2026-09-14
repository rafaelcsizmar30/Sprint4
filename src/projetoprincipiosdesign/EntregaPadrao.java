package projetoprincipiosdesign;

public class EntregaPadrao implements Entrega {
    @Override
    public boolean isDisponivel(double total) {
        return true;
    }

    @Override
    public double calcularFrete(double total) {
        return 15.0;
    }
}
