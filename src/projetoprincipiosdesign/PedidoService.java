package projetoprincipiosdesign;

public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public double calcularTotal(Pedido pedido, CalculadoraDesconto calculadoraDesconto) {
        double total = 0.0;

        for (ItemPedido item : pedido.getItens()) {
            total += item.getPreco() * item.getQuantidade();
        }

        return calculadoraDesconto.aplicar(total);
    }

    public String obterCidadeEntrega(Pedido pedido) {
        return pedido.getCliente().getCidadeEntrega();
    }

    public void finalizarPedido(Pedido pedido, CalculadoraDesconto calculadoraDesconto, IPagamento pagamento) {
        double total = calcularTotal(pedido, calculadoraDesconto);

        pedidoRepository.salvar(pedido.getCliente().getNome(), total);

        System.out.println("Gerando resumo do pedido...");
        System.out.println("Cliente: " + pedido.getCliente().getNome());
        System.out.printf("Total: R$ %.2f%n", total);

        pagamento.pagar(total);

        System.out.println(
            "Enviando mensagem para " + pedido.getCliente().getNome() + ": pedido finalizado."
        );
    }
}
