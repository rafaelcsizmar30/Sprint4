package projetoprincipiosdesign;

public class DescontoFuncionario implements CalculadoraDesconto {
    @Override
    public double aplicar(double valor) {
        return valor * 0.80;
    }
}
