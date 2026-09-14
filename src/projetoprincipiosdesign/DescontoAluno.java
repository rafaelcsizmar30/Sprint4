package projetoprincipiosdesign;

public class DescontoAluno implements CalculadoraDesconto {
    @Override
    public double aplicar(double valor) {
        return valor * 0.90;
    }
}
