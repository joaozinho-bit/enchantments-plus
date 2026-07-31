# Enchantments Plus - Enchantments

Este documento descreve todos os encantamentos atualmente planeados para o Enchantments Plus.

Cada encantamento deverá ser equilibrado, configurável e compatível com multiplayer.

Todos os valores apresentados são apenas referências e poderão ser alterados durante o desenvolvimento.

---

# Eternal

## Aplicável a

- Espadas
- Machados
- Picaretas
- Pás
- Enxadas
- Arcos
- Bestas
- Tridentes
- Elytra
- Escudos
- Armaduras

## Descrição

Remove completamente o sistema de durabilidade do item.

O item deixa de sofrer qualquer desgaste.

Não utiliza o sistema vanilla de **Unbreakable**.

## Comportamento

Enquanto possuir este encantamento:

- nunca perde durabilidade
- não apresenta barra de durabilidade
- não apresenta informação de durabilidade no tooltip (F3 + H)
- funciona normalmente em todos os restantes aspetos

O objetivo é que o item pareça não possuir durabilidade.

## Configuração

- ativado
- custo
- tesouro
- nível máximo

---

# Storm

## Aplicável a

- Espadas

## Descrição

Golpes críticos possuem uma probabilidade de invocar um relâmpago na entidade atingida.

## Configuração

- probabilidade
- dano
- nível máximo

---

# Vampirism

## Aplicável a

- Espadas

## Descrição

Recupera vida sempre que o jogador causa dano a uma entidade.

A quantidade de vida recuperada corresponde a uma percentagem do dano causado.

Não existe probabilidade.

Todos os ataques podem recuperar vida.

## Configuração

- percentagem de cura
- nível máximo

---

# Momentum

## Aplicável a

- Picaretas
- Pás

## Descrição

Cada bloco partido consecutivamente aumenta temporariamente a velocidade de mineração.

Ao deixar de minerar durante algum tempo, o efeito desaparece.

## HUD

Mostra o multiplicador atual.

Exemplo:

```
⛏ x18
```

## Configuração

- incremento por bloco
- tempo antes de reiniciar
- limite máximo
- nível máximo

---

# Telekinesis

## Aplicável a

- Ferramentas
- Armas

## Descrição

Os itens obtidos são enviados diretamente para o inventário.

Caso não exista espaço suficiente, caem normalmente no chão.

No futuro poderá suportar filtros configuráveis.

## Configuração

- ativado
- filtros
- nível máximo

---

# Wither

## Aplicável a

- Espadas

## Descrição

Aplica o efeito Wither às entidades atingidas.

Os níveis aumentam a duração.

## Configuração

- duração
- amplificador
- nível máximo

---

# Attack Speed

## Aplicável a

- Espadas
- Armas do Simply Swords

## Descrição

Cria períodos temporários durante os quais o jogador consegue atacar mais rapidamente.

Após terminar o efeito entra em cooldown.

Não altera permanentemente o atributo Attack Speed.

## HUD

Exemplo:

```
⚡ 5.2s
```

## Configuração

- duração
- cooldown
- multiplicador de velocidade
- nível máximo

---

# Burning Protection

## Aplicável a

- Armaduras

## Descrição

Só funciona quando as quatro peças possuem este encantamento.

Enquanto o conjunto estiver completo:

- imunidade ao fogo
- imunidade à lava
- imunidade a dano por queimadura

## Configuração

- ativado
- nível máximo

---

# Hearty

## Aplicável a

- Armaduras

## Descrição

Cada peça equipada aumenta a vida máxima do jogador.

O efeito acumula entre todas as peças.

## Configuração

- vida adicional por peça
- limite máximo
- nível máximo

---

# Jump

## Aplicável a

- Botas

## Descrição

Permite carregar um salto mantendo pressionada uma tecla.

Quanto maior o carregamento, maior será o salto.

O dano de queda provocado por esse salto é completamente anulado.

## HUD

Durante o carregamento:

```
██████░░░░
```

Após o salto desaparece automaticamente.

## Configuração

- altura máxima
- velocidade de carregamento
- tecla
- nível máximo

---

# Encantamentos Futuros

## Lightning Dash

Dash elétrico.

Ainda em fase de design.

---

## Wind Step

Aumenta a velocidade de movimento.

Ainda em fase de design.

---

## Excavator

Parte uma área 3×3.

Ainda por definir.

---

# Encantamentos Não Planeados

Os seguintes encantamentos não estão atualmente previstos para implementação.

## Tree Feller

Não será implementado.

O objetivo é manter compatibilidade com o mod FallingTree.

## Vein Miner

Não será implementado.

Está previsto um futuro mod dedicado à mineração em massa.