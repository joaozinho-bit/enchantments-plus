# Enchantments Plus - HUD System

## Objetivo

O sistema de HUD deverá fornecer indicadores discretos para encantamentos que possuam estados temporários.

A prioridade é informar o jogador sem alterar a experiência visual do Minecraft.

O HUD nunca deverá parecer pertencente a outro jogo.

---

# Filosofia

O HUD deverá ser:

- minimalista
- discreto
- consistente
- reutilizável
- pouco intrusivo

Sempre que existir dúvida entre mostrar ou esconder informação, deverá ser privilegiada uma interface mais limpa.

---

# Encantamentos que utilizam HUD

Exemplos atuais:

- Attack Speed
- Momentum
- Jump

No futuro poderão existir outros.

Nem todos os encantamentos deverão possuir indicadores.

---

# Encantamentos sem HUD

Exemplos:

- Eternal
- Storm
- Vampire
- Wither
- Telekinesis

O efeito já é suficientemente percetível durante o jogo.

---

# Sistema Genérico

O HUD não deverá conhecer nenhum encantamento específico.

Cada encantamento apenas fornece informação sobre o estado atual.

O sistema decide:

- quando mostrar
- onde mostrar
- ordem
- animações
- desaparecimento

---

# Informação

Cada indicador poderá fornecer:

- ícone
- nome
- tempo restante
- progresso
- nível
- prioridade

Nem todos os campos são obrigatórios.

---

# Localização

Os indicadores deverão ocupar uma pequena área do ecrã.

Nunca deverão interferir com:

- barra de vida
- fome
- experiência
- hotbar
- chat

A posição deverá ser facilmente configurável no futuro.

---

# Quantidade Máxima

O HUD nunca deverá crescer indefinidamente.

Caso existam muitos efeitos:

- mostrar apenas os mais importantes
- esconder efeitos pouco relevantes
- ordenar por prioridade

---

# Prioridades

Exemplo:

Alta

- efeitos ativos

Média

- carregamentos

Baixa

- cooldowns

Muito baixa

- efeitos permanentes

---

# Tipos de Indicadores

O sistema deverá suportar diferentes tipos.

## Temporizador

Exemplo:

Attack Speed

```
⚡ 5.2s
```

---

## Contador

Momentum

```
⛏ x18
```

---

## Barra de Carregamento

Jump

```
██████░░░░
```

A barra apenas existe enquanto o jogador está a carregar o salto.

---

## Estado

Futuras possibilidades:

```
Ready

Charging

Cooldown
```

---

# Atualização

Os indicadores deverão desaparecer automaticamente quando deixam de ser relevantes.

Evitar manter informação visível durante demasiado tempo.

---

# Animações

As animações deverão ser muito subtis.

Exemplos:

- fade in
- fade out
- ligeiro movimento

Nunca utilizar animações exageradas.

---

# Configuração

No futuro o jogador poderá configurar:

- posição
- escala
- opacidade
- quantidade máxima
- mostrar cooldowns
- mostrar nomes
- mostrar apenas ícones

---

# Extensibilidade

Adicionar um novo encantamento ao HUD deverá exigir apenas que esse encantamento forneça o estado atual.

Nenhuma alteração ao renderer deverá ser necessária.

---

# Objetivo

O sistema deverá parecer uma funcionalidade nativa do Minecraft.

Mesmo com vários encantamentos ativos, o ecrã deverá manter-se limpo, organizado e fácil de ler.