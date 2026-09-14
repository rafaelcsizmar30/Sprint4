package projetoprincipiosdesign;

public class SemDesconto implements CalculadoraDesconto {
    @Override
    public double aplicar(double valor) {
        return valor;
    }
}
