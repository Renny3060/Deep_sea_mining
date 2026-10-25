# 🌊 Deep Sea Mining

[![Licença: MIT](https://img.shields.io/badge/Licen%C3%A7a-MIT-green.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-25-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)

Jogo de exploração submarina em **Java + JavaFX**. Comandas um navio de mineração à superfície e lanças drones por um fosso até ao fundo do mar, para recolher minérios e resgatar artefactos antigos, gerindo combustível e danos pelo caminho.

Trabalho prático desenvolvido no ISEC (Grupo 37). A lógica do jogo é modelada como uma **máquina de estados finita (FSM)**, separada da interface gráfica.

---

## 🎮 Como se joga

1. **À superfície**, movimentas o navio pela grelha. O navio gasta combustível a cada movimento.
2. Na **oficina** (com o navio parado) podes abastecer drones, repará-los e melhorá-los (tanque ou blindagem) usando minérios.
3. **Lanças um drone** para o fosso. Durante a descida desviás-lo lateralmente de rochas, animais marinhos e correntes.
4. No **fundo do mar** (grelha 10×10) exploras célula a célula, recolhes minérios e evitas monstros.
5. Ao encontrares um **artefacto**, tens de resolver um **puzzle de 15 peças** (grelha 4×4) com um número limitado de movimentos. Se falhares, o drone sobe sem o artefacto.
6. O drone **sobe de volta ao navio** e entrega o que recolheu.

**Ganhas** quando recolhes todos os artefactos. **Perdes** se o navio ficar sem combustível ou se perderes todos os drones.

### Controlos de teclado

| Fase | Teclas |
|---|---|
| Superfície | `↑` `↓` `←` `→` movem o navio |
| Descida pelo fosso | `←` `→` movem o drone lateralmente; `↓`, `Enter` ou `Espaço` avançam para o fundo |
| Subida pelo fosso | `←` `→` movem o drone lateralmente; `↑`, `Enter` ou `Espaço` regressam ao navio |
| Fundo do mar, puzzle, oficina | Rato / botões da interface |

### Parâmetros do jogo

Definidos em `Constants.java`:

| Parâmetro | Valor |
|---|---|
| Grelha da superfície | 15 × 30 |
| Fosso | 20 de profundidade × 10 de largura |
| Fundo do mar | 10 × 10 |
| Combustível do navio | 1000 |
| Drones iniciais | 3 (combustível 100, casco 100) |
| Artefactos a recolher | 5 |
| Puzzle | 4 × 4, máximo de 100 movimentos |
| Consumo: movimento do navio / passo do drone | 5 / 1 |
| Reparação do casco | 20 de combustível |
| Dano: animal marinho / monstro | 10 / 5 |
| Melhorias da oficina | +20 de tanque, +10 de blindagem |

---

## 📋 Requisitos

| Requisito | Notas |
|---|---|
| JDK 25 | Versão definida no `pom.xml` |
| Maven 3.9+ | Ou usa o Maven integrado no IntelliJ IDEA |

Dependências geridas pelo Maven: JavaFX 21.0.2 (`controls` e `media`) e JUnit 5.10.2 (testes).

---

## 🚀 Como executar

```bash
# Executar o jogo
mvn javafx:run

# Correr os testes
mvn test
```

No IntelliJ IDEA, abre a pasta como projeto Maven e usa as configurações de execução incluídas (`DeepSeeApp`).

---

## 🏗️ Arquitetura

O projeto separa **modelo** (regras do jogo) e **interface** (JavaFX). A UI nunca altera os dados diretamente: fala sempre com o `DeepSeaManager`.

### Máquina de estados

```
SETUP → IDLE ⇄ DESCEND → EXPLORE ⇄ PUZZLE
          ↑                 │
          └──── ASCEND ←────┘

IDLE / DESCEND / EXPLORE / ASCEND  →  GAME_OVER
ASCEND (todos os artefactos)       →  WIN
```

| Estado | Descrição |
|---|---|
| `SETUP_STATE` | Preparação inicial do jogo |
| `IDLE_STATE` | Navio à superfície: mover, abastecer, reparar, melhorar e lançar drones |
| `DESCEND_STATE` | Drone a descer pelo fosso |
| `EXPLORE_STATE` | Drone no fundo do mar: explorar, recolher minério ou artefacto |
| `PUZZLE_STATE` | Mini-jogo para recolher um artefacto |
| `ASCEND_STATE` | Drone a subir de volta ao navio |
| `WIN_STATE` | Todos os artefactos recolhidos |
| `GAME_OVER_STATE` | Sem combustível ou sem drones |

O diagrama detalhado está em [`reports/DeepSea_State_Machine.pdf`](reports/DeepSea_State_Machine.pdf).

### Padrões de desenho

- **State** — `IDeepSeaState` define todas as transições; cada estado concreto (`model/fsm/states/`) implementa as que são válidas nesse momento. `DeepSeaStateAdapter` fornece o comportamento por defeito.
- **Factory Method** — `DeepSeaState.createState(...)` cria a instância concreta de cada estado.
- **Singleton** — `DeepSeaLog` mantém o registo histórico de eventos do jogo.

### Estrutura do código

```
src/main/java/pt/isec/pa/deepsea/
├── DeepSeeApp.java        # Ponto de entrada JavaFX e teclas
├── model/
│   ├── data/              # Entidades e regras: Ship, Drone, SeaFloor, SeaTrench,
│   │                      #   MiniGame, Artifact, células, Constants, DeepSeaMining
│   └── fsm/               # DeepSeaManager, DeepSeaContext, DeepSeaState, DeepSeaLog
│       └── states/        # Um ficheiro por estado
└── ui/                    # Interface JavaFX: RootPane, canvases, painéis, menus
    ├── res/ e util/       # Gestão de imagens e CSS
src/main/resources/        # css/style.css e images/
src/test/java/             # Testes JUnit 5
reports/                   # Documentação (diagrama da máquina de estados)
```

---

## 💾 Ficheiros gerados pelo jogo

| Ficheiro | Para que serve |
|---|---|
| `deepsea_save.dat` | Gravação binária do jogo |
| `deepsea_history.txt` | Registo histórico de eventos (também consultável na janela de logs) |

---

## 🧪 Testes

Testes unitários com JUnit 5 para o modelo (`Ship`, `Drone`, `SeaFloor`, `SeaTrench`, `MiniGame`, `DeepSeaMining`, `DeepSeaManager`) e para a aplicação.

```bash
mvn test
```

---

## 👥 Autores

Grupo 37 — ISEC.

## 📄 Licença

Distribuído sob a **Licença MIT**. Consulta o ficheiro [LICENSE](LICENSE).
