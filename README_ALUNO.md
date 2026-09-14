# Laboratório de Refatoração — Princípios de Projeto

## Contexto

Você recebeu um pequeno sistema de pedidos que **funciona**, mas foi desenvolvido sem grande preocupação com princípios de projeto.

Seu objetivo é analisar e refatorar o código **sem alterar o comportamento essencial da aplicação**.

Os princípios trabalhados são:

1. Responsabilidade Única
2. Segregação de Interfaces
3. Inversão de Dependências / Prefira Interfaces a Classes
4. Prefira Composição à Herança
5. Princípio de Demeter
6. Aberto/Fechado
7. Substituição de Liskov

## Como executar

Pré-requisito: **JDK 17 ou superior**.

Na pasta raiz do projeto, compile:

```bash
javac -d out src/projetoprincipiosdesign/*.java
```

Depois execute:

```bash
java -cp out projetoprincipiosdesign.Main
```

## Sua missão

Para cada princípio:

- identifique pelo menos um trecho problemático;
- explique por que o trecho merece ser refatorado;
- proponha uma solução;
- implemente a alteração;
- execute novamente o projeto.

## Regras

- Não remova funcionalidades apenas para facilitar a refatoração.
- Não é necessário que sua solução fique igual à de outros colegas.
- Justifique cada decisão.
- Princípios de projeto são recomendações: evite criar complexidade sem necessidade.
- Preserve a possibilidade de cadastrar, calcular, pagar e apresentar o pedido.

## Justificativas da refatoração

### 1. Responsabilidade Única (SRP)

**Trecho problemático:** o método `finalizarPedido` em `PedidoService` fazia cálculo do total, salvamento em arquivo, geração de resumo e execução do pagamento — quatro responsabilidades diferentes na mesma classe.

**Por que refatorar:** uma classe com várias responsabilidades muda por vários motivos diferentes (mudar o formato de armazenamento, mudar o cálculo, mudar o resumo), o que aumenta o risco de um ajuste quebrar outro comportamento não relacionado.

**Solução:** extraí a responsabilidade de persistência para uma nova classe, `PedidoRepository`, com o método `salvar(nomeCliente, total)`. `PedidoService` passou a apenas orquestrar o fluxo, delegando o salvamento.

**Implementação:** classe `PedidoRepository.java` criada; `PedidoService.finalizarPedido` agora chama `pedidoRepository.salvar(...)` em vez de manipular arquivos diretamente.

---

### 2. Segregação de Interfaces (ISP)

**Trecho problemático:** a interface `IPagamento` original tinha três métodos (`pagar`, `parcelar`, `gerarBoleto`), mas nem toda forma de pagamento usava todos. `PagamentoPix`, por exemplo, lançava `UnsupportedOperationException` em `parcelar`, e `PagamentoCartao`/`PagamentoBoleto` tinham métodos com comentário "Operação não utilizada".

**Por que refatorar:** forçar uma classe a implementar métodos que não fazem sentido para ela viola o ISP e obriga a lidar com exceções ou implementações vazias apenas para satisfazer o contrato da interface.

**Solução:** dividi `IPagamento` em três interfaces menores e coesas: `IPagamento` (pagar), `IParcelavel` (parcelar) e `IGeraBoleto` (gerarBoleto).

**Implementação:** `PagamentoCartao implements IPagamento, IParcelavel`; `PagamentoPix implements IPagamento` (sem mais o método de parcelamento nem a exceção); `PagamentoBoleto implements IPagamento, IGeraBoleto`.

---

### 3. Inversão de Dependências / Prefira Interfaces a Classes (DIP)

**Trecho problemático:** `finalizarPedido` decidia internamente, por meio de `if/else` sobre uma `String formaPagamento`, qual classe concreta de pagamento instanciar (`new PagamentoCartao()`, `new PagamentoPix()`, etc.).

**Por que refatorar:** `PedidoService` (módulo de alto nível) dependia diretamente das implementações concretas de pagamento (módulo de baixo nível), em vez de depender de uma abstração. Isso também violava o Aberto/Fechado (ver item 6).

**Solução:** `PedidoService` passou a depender apenas da abstração `IPagamento`, recebida como parâmetro. Quem monta o pagamento concreto é a camada que utiliza o serviço (`Main`), não o serviço em si.

**Implementação:** `finalizarPedido(Pedido pedido, CalculadoraDesconto calculadoraDesconto, IPagamento pagamento)` — a instância concreta (`new PagamentoCartao()`) é criada em `Main` e injetada.

---

### 4. Prefira Composição à Herança

**Trecho problemático:** `PedidoService extends PagamentoCartao`. Um serviço de pedidos herdava de uma forma de pagamento específica só para "aproveitar" o método `pagar`.

**Por que refatorar:** herança deveria representar uma relação "é um". `PedidoService` não é um tipo de `PagamentoCartao` — a herança aqui existia só por conveniência de acesso a um método, criando acoplamento desnecessário e impedindo o uso de outras formas de pagamento sem duplicar código.

**Solução:** troquei a herança por composição: `PedidoService` passou a ter (usar) um `IPagamento`, em vez de ser um.

**Implementação:** `public class PedidoService` (sem `extends`), recebendo `IPagamento` como parâmetro do método `finalizarPedido`.

---

### 5. Princípio de Demeter (Lei do Menor Conhecimento)

**Trecho problemático:** `obterCidadeEntrega` fazia `pedido.getCliente().getEndereco().getCidade().getNome()` — um encadeamento que obriga `PedidoService` a conhecer a estrutura interna de `Cliente`, `Endereco` e `Cidade`.

**Por que refatorar:** esse encadeamento acopla `PedidoService` a detalhes internos de três classes diferentes. Se a estrutura de `Endereco` mudar (por exemplo, adicionar um objeto `Regiao` entre `Endereco` e `Cidade`), o `PedidoService` também precisaria mudar, mesmo não sendo o dono dessa regra.

**Solução:** cada classe passou a "esconder" o próximo nível, expondo um método de mais alto nível: `Endereco.getNomeCidade()` e `Cliente.getCidadeEntrega()`.

**Implementação:** `PedidoService.obterCidadeEntrega` agora é só `pedido.getCliente().getCidadeEntrega()` — uma única chamada, sem atravessar objetos internos.

---

### 6. Aberto/Fechado (OCP)

**Trecho problemático:** `calcularTotal` tinha um `if/else` verificando `tipoCliente` (`"ALUNO"`, `"PROFESSOR"`, `"FUNCIONARIO"`) para decidir o percentual de desconto.

**Por que refatorar:** para cada novo tipo de cliente/desconto, seria necessário abrir e modificar `PedidoService`, aumentando o risco de quebrar um cálculo já validado só para adicionar um novo caso.

**Solução:** criei a abstração `CalculadoraDesconto` (interface com o método `aplicar(valor)`) e uma implementação por tipo de desconto: `DescontoAluno`, `DescontoProfessor`, `DescontoFuncionario`, `SemDesconto`. Um novo tipo de desconto passa a exigir apenas uma nova classe, sem tocar em `PedidoService`.

**Implementação:** `calcularTotal(Pedido pedido, CalculadoraDesconto calculadoraDesconto)` apenas aplica a estratégia recebida — `PedidoService` ficou fechado para modificação e aberto para extensão.

---

### 7. Substituição de Liskov (LSP)

**Trecho problemático:** `EntregaRetiradaLoja extends Entrega` e sobrescrevia `calcularFrete` lançando `IllegalStateException` quando o total era menor que R$ 50. Quem usasse a referência do tipo `Entrega` esperando apenas um cálculo de frete seria surpreendido por uma exceção não prevista pelo tipo base.

**Por que refatorar:** uma subclasse não pode restringir o contrato da classe base a ponto de quebrar código que funciona corretamente com a superclasse. `EntregaRetiradaLoja` não podia substituir `Entrega` em qualquer situação sem risco de comportamento inesperado.

**Solução:** transformei `Entrega` em uma interface com dois métodos: `isDisponivel(total)` e `calcularFrete(total)`. Em vez de lançar exceção, `EntregaRetiradaLoja.isDisponivel` informa explicitamente quando a opção não pode ser usada, e quem for calcular o frete verifica essa condição antes de chamar `calcularFrete`.

**Implementação:** `Entrega` (interface), `EntregaPadrao` (frete fixo de R$ 15, sempre disponível) e `EntregaRetiradaLoja` (frete R$ 0, disponível apenas a partir de R$ 50) — ambas cumprem o mesmo contrato sem exceções inesperadas.
