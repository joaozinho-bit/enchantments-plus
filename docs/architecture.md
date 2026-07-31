# Enchantments Plus - Architecture

## Objetivo

A arquitetura do Enchantments Plus foi desenhada para permitir a adição de novos encantamentos durante vários anos sem que o código se torne difícil de manter.

Sempre que possível, deverá ser privilegiada reutilização em vez de duplicação de código.

Cada novo encantamento deverá necessitar apenas da sua própria lógica específica.

Todo o resto deverá ser fornecido pela infraestrutura do mod.

---

# Princípios

A arquitetura deverá seguir os seguintes princípios:

- Single Responsibility
- Baixo acoplamento
- Alta reutilização
- Fácil manutenção
- Fácil configuração
- Performance elevada

Nenhum sistema deverá depender diretamente de outro quando isso puder ser evitado.

---

# Estrutura

Uma possível organização:

```
enchantmentsplus/

    EnchantmentsPlus.java

    registry/
    enchantments/
    effects/
    config/
    events/
    hud/
    compatibility/
    util/
    networking/
```

A estrutura poderá evoluir ao longo do desenvolvimento.

No entanto, cada responsabilidade deverá possuir o seu próprio pacote.

---

# Encantamentos

Cada encantamento deverá possuir a sua própria classe.

Exemplo:

```
EternalEnchantment

StormEnchantment

MomentumEnchantment

JumpEnchantment
```

Nunca deverá existir uma única classe com toda a lógica dos encantamentos.

---

# Registry

Todos os encantamentos deverão ser registados através de um único sistema de Registry.

Adicionar um novo encantamento deverá exigir apenas:

- criar a classe
- registá-la
- adicionar traduções
- adicionar configuração

Sem alterar vários sistemas diferentes.

---

# Configuração

Cada encantamento deverá possuir uma configuração independente.

Exemplos:

- ativado
- nível máximo
- cooldown
- duração
- dano
- multiplicadores
- probabilidades

Os valores nunca deverão ficar hardcoded quando fizer sentido serem configuráveis.

---

# Eventos

Os eventos deverão ser separados por responsabilidade.

Exemplos:

- ataque
- mineração
- dano
- morte
- equipamento
- tick
- inventário

Evitar grandes classes que tratem todos os eventos.

---

# Sistema de Efeitos

Os encantamentos que possuem estados temporários deverão utilizar um sistema comum.

Exemplos:

- Attack Speed
- Momentum
- Jump
- futuros encantamentos

Esse sistema deverá gerir:

- duração
- cooldown
- estado
- sincronização
- HUD

Os encantamentos apenas deverão definir o comportamento do efeito.

---

# HUD

O sistema de HUD deverá ser completamente independente dos encantamentos.

Cada encantamento apenas informa:

- se pretende mostrar um indicador
- ícone
- nome
- duração
- prioridade

Todo o desenho deverá ser responsabilidade do sistema de HUD.

---

# Compatibilidade

O código específico para outros mods deverá ficar isolado.

Exemplo:

```
compatibility/

    BetterEnchant

    SimplySwords
```

Nunca espalhar verificações de compatibilidade pelo projeto.

---

# Networking

Sempre que um encantamento necessite de sincronização cliente-servidor deverá utilizar um sistema comum.

Evitar criar packets exclusivos quando um sistema reutilizável resolver o problema.

---

# Utilitários

Métodos reutilizáveis deverão ficar separados da lógica dos encantamentos.

Exemplos:

- cálculos
- inventário
- entidades
- partículas
- sons

---

# Performance

Os encantamentos deverão evitar:

- loops desnecessários
- criação excessiva de objetos
- verificações em todos os ticks quando não forem necessárias

Sempre que possível utilizar eventos em vez de polling.

---

# Escalabilidade

A arquitetura deverá permitir adicionar dezenas de novos encantamentos sem necessidade de alterações profundas.

Se adicionar um novo encantamento obrigar a modificar muitas classes existentes, provavelmente existe um problema de arquitetura.

---

# Objetivo

Criar uma infraestrutura suficientemente genérica para que desenvolver um novo encantamento seja simples, previsível e consistente com o resto do projeto.