# Enchantments Plus

## Visão do Projeto

Enchantments Plus é um mod para Minecraft Fabric 1.21.1 que adiciona novos encantamentos ao jogo, mantendo uma experiência totalmente integrada com o vanilla.

O objetivo não é adicionar dezenas ou centenas de encantamentos, mas sim criar um conjunto pequeno de encantamentos únicos, interessantes, equilibrados e altamente polidos.

Cada encantamento deve parecer que poderia existir no Minecraft oficial.

O mod deve privilegiar qualidade, consistência e facilidade de manutenção acima da quantidade de conteúdo.

---

# Filosofia

Todos os encantamentos devem seguir estes princípios:

- Vanilla+
- Bem equilibrados
- Divertidos de utilizar
- Compatíveis com multiplayer
- Compatíveis com mods populares
- Configuráveis
- Performance elevada
- Código limpo
- Fácil expansão

Sempre que existir uma escolha entre adicionar mais funcionalidades ou manter uma experiência mais limpa, deve ser privilegiada a segunda opção.

---

# Objetivos

O mod deverá:

- adicionar encantamentos originais
- reutilizar sempre que possível os sistemas vanilla
- evitar mecânicas demasiado complexas
- permitir configuração completa através de ficheiros de configuração
- funcionar tanto em singleplayer como em servidores Fabric

---

# Compatibilidade

O mod deverá funcionar de forma independente.

No entanto, foi desenhado para integrar perfeitamente com:

- Better Enchant
- Simply Swords

Quando o Better Enchant estiver instalado, todos os encantamentos deverão aparecer automaticamente na interface sem necessidade de código específico para cada um.

---

# Arquitetura

O projeto deverá ser preparado para crescer durante vários anos.

Não deverá existir uma única classe responsável por toda a lógica.

Cada encantamento deverá possuir a sua própria implementação.

Os sistemas reutilizáveis deverão ser abstraídos sempre que possível.

Exemplos:

- Registry
- Configuração
- Eventos
- HUD
- Efeitos temporários
- Utilitários

Sempre que seja criado um sistema novo, este deve ser suficientemente genérico para poder ser reutilizado por futuros encantamentos.

---

# Configuração

Todos os encantamentos deverão poder ser configurados.

Exemplos:

- ativar/desativar
- nível máximo
- cooldown
- duração
- probabilidades
- dano
- multiplicadores
- compatibilidades
- efeitos

Os valores nunca deverão ficar hardcoded quando fizer sentido serem configuráveis.

---

# Interface

A interface deve respeitar a filosofia visual do Minecraft.

Não devem existir janelas complexas, barras gigantes ou elementos que ocupem demasiado espaço no ecrã.

A prioridade é manter uma experiência limpa e minimalista.

Sempre que possível deve parecer uma funcionalidade vanilla.

---

# Sistema de HUD

Alguns encantamentos poderão possuir efeitos temporários.

Exemplos:

- Attack Speed
- Momentum
- Jump
- futuros encantamentos

Nestes casos poderá ser apresentado um pequeno indicador no HUD.

No entanto:

- apenas quando necessário
- discreto
- minimalista
- consistente entre todos os encantamentos

O objetivo é informar o jogador sem encher o ecrã.

Nunca deverão existir elementos redundantes.

O sistema deverá ser completamente genérico para que futuros encantamentos possam reutilizá-lo.

---

# Encantamentos

## Eternal

Pode ser aplicado em:

- ferramentas
- armas
- armaduras
- escudo
- elytra

O item nunca perde durabilidade.

Não utiliza o sistema vanilla de Unbreakable.

A barra de durabilidade continua visível.

---

## Storm

Espadas.

Golpes críticos possuem uma probabilidade de invocar um relâmpago sobre a entidade atingida.

---

## Vampire

Espadas.

Recupera uma percentagem da vida correspondente ao dano causado.

---

## Momentum

Picaretas e pás.

Cada bloco partido consecutivamente aumenta temporariamente a velocidade de mineração.

Ao interromper a mineração durante algum tempo o efeito desaparece.

---

## Telekinesis

Ferramentas e armas.

Os itens recolhidos são enviados diretamente para o inventário.

Caso não exista espaço disponível, caem normalmente no chão.

No futuro poderá suportar filtros configuráveis.

---

## Wither

Espadas.

Aplica o efeito Wither às entidades atingidas.

Os níveis aumentam a duração do efeito.

---

## Attack Speed

Espadas.

Compatível também com armas do Simply Swords.

Em vez de aumentar permanentemente o atributo Attack Speed, cria períodos temporários de ataques rápidos.

Durante esse período:

- maior velocidade de ataque
- indicador discreto no HUD
- no final entra em cooldown

O objetivo é criar momentos de combate mais interessantes e estratégicos do que simplesmente aumentar um atributo.

---

## Burning Protection

Armadura.

Apenas funciona quando as quatro peças possuem o encantamento.

Enquanto o conjunto estiver completo:

- imunidade ao fogo
- imunidade à lava
- imunidade a dano por queimadura

É um encantamento claramente orientado para end-game.

---

## Hearty

Armadura.

Cada peça equipada aumenta a vida máxima do jogador.

Os valores serão configuráveis.

---

## Jump

Botas.

Ao manter pressionada uma tecla é carregado um salto.

Quanto maior o carregamento, maior será o salto.

O dano de queda provocado por esse salto é completamente anulado.

O carregamento deverá utilizar um pequeno indicador temporário no HUD.

---

# Traduções

Todo o texto deverá utilizar translation keys.

Nunca utilizar texto hardcoded.

Idiomas previstos:

- en_us
- pt_pt

---

# Objetivo Final

O Enchantments Plus pretende tornar-se uma referência entre os mods de encantamentos para Fabric.

Todas as novas funcionalidades deverão seguir a mesma filosofia:

- simples
- elegantes
- equilibradas
- configuráveis
- reutilizáveis
- fáceis de manter

Sempre que existir dúvida sobre uma decisão de implementação, deverá ser escolhida a solução mais consistente com a experiência vanilla e com a arquitetura do projeto.