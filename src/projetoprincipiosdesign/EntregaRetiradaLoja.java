package projetoprincipiosdesign;

public class EntregaRetiradaLoja implements Entrega {

    private static final double VALOR_MINIMO = 50.0;

    @Override
    public boolean isDisponivel(double total) {
        return total >= VALOR_MINIMO;
    }

    @Override
    public double calcularFrete(double total) {
        return 0.0;
    }
}
