package projetoprincipiosdesign;

public class DescontoProfessor implements CalculadoraDesconto {
    @Override
    public double aplicar(double valor) {
        return valor * 0.85;
    }
}
