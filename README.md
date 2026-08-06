# 🎯 DanaQuests

![Minecraft Paper 1.21](https://img.shields.io/badge/Minecraft-Paper%201.21.x-brightgreen?style=for-the-badge&logo=minecraft)
![Java 21](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Build Maven](https://img.shields.io/badge/Build-Maven-blue?style=for-the-badge&logo=apachemaven)
![Version 5.0.2](https://img.shields.io/badge/Version-5.0.2-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-GPL--3.0-lightgrey?style=for-the-badge)

**DanaQuests** est un plugin de quêtes Minecraft complet, moderne et modulaire développé pour l'écosystème **DanaKube** (basé sur un fork d'ExcellentQuests). Il réinvente l'expérience de jeu en introduisant 4 grands systèmes de quêtes complémentaires (Lore narratif, Îles coopératives, RPG personnel et Événements communautaires) ainsi qu'un moteur de suivi unifié multi-canal.

---

## 🚀 Nouveautés & Optimisations (v5.0.2)

* 🔄 **Unification du TaskManager** : Traitement centralisé de tous les objectifs de quêtes (Lore, Personnelles, Daily) directement via `TaskManager#progressQuests()`.
* 🧩 **Matching Intelligents & Alias d'Items** : Résolution automatique des écarts Spigot entre noms d'items et de blocs (ex: `carrot` $\leftrightarrow$ `carrots`, `potato` $\leftrightarrow$ `potatoes`, `beetroot` $\leftrightarrow$ `beetroots`).
* 🍱 **Positionnement Dynamique (`SlotsByCount`)** : Repositionnement automatique des icônes dans les menus de quêtes en fonction du nombre d'éléments à afficher.
* 📦 **Mobs Empilés & Groupes Génériques** : Prise en charge des entités empilées (*stacked mobs*) et des groupes de ressources (`resource_groups`).

---

## ✨ Fonctionnalités Principales

### 📖 1. Quêtes de Lore (Histoire Scénarisée & Linéaire)
* 🗺️ **Progression Linéaire** : Quêtes organisées en suites (Questlines) avec système de prérequis (`prerequisite`).
* 📜 **Catégorisation Dynamique** : Fichiers YAML modulaires par chapitre (ex: `lore/decouverte.yml`, `lore/ere1.yml`).
* 🎯 **Objectifs Étendus (TaskTypes)** :
  * 🗣️ **PNJ** : Interaction avec PNJ via **Citizens** (`NPCRightClickEvent`) et **FancyNPCs** (`NpcInteractEvent`).
  * 📍 **Lieux** : Détection par zones **WorldGuard**, coordonnées avec rayon, biomes spécifiques ou structures Vanilla (Mineshaft, etc.).
  * 🪵 **Écorçage** (`STRIP_LOG`) : Écorcer des bûches avec une hache.
  * 🧪 **Désoxydation** (`DEOXIDIZE_COPPER`) : Retirer l'oxydation du cuivre avec une hache.
  * 🧪 **Consommation** (`CONSUME_ITEM`) : Manger des aliments ou consommer des potions.
  * 🤝 **Commerce PNJ** (`TRADE_WITH_VILLAGER`) : Réaliser des échanges avec des marchands (`PlayerTradeEvent`).
  * 🍯 **Récolte** (`HARVEST_ITEM`) : Récolter du miel, composter pour de la poudre d'os ou cueillir des baies.
  * 💻 **Commande** (`EXECUTE_COMMAND`) : Exécuter une commande joueur (ex: `/spawn`, `/danatool`).

### 🏝️ 2. Quêtes d'Île Coopératives (SuperiorSkyblock2)
* 🏝️ **Progression Partagée** : Liaison directe avec l'API **SuperiorSkyblock2** (SSB2) ; la progression est sauvegardée au niveau de l'île (`excellentquests_islands`).
* 📦 **Groupes de Ressources & Poids** (`resource_groups.yml`) : Regroupement de matériaux (ex: `wood`, `ores`) avec attribution de multiplicateurs de valeur (ex: 1 Diamant = 5 points, 1 Charbon = 1 point).
* 📥 **GUI de Dépôt Interactif (Option B)** : Clic gauche pour déposer un stack, Clic droit pour vider l'inventaire. Algorithme anti-gaspillage qui ne consomme que le strict nécessaire pour compléter l'objectif.
* 🔒 **Verrou Anti-Concurrence** : Protection mémoire (RAM) empêchant deux membres d'une même île d'ouvrir le GUI de dépôt en même temps.

### ⚔️ 3. Quêtes Personnelles RPG (Progression par Catégories)
* 🧙 **Choix de Catégorie** : Le joueur sélectionne la catégorie qu'il souhaite accomplir pour la journée (Minage, Combat, etc.) dans `personal.yml`.
* 🎯 **Tirage Aléatoire** : Le serveur sélectionne une cible au hasard parmi celles configurées dans la catégorie et l'affiche avec ses exigences scalées dans `personal_categories.yml`.
* 📈 **Échelle Quadratique** : Difficulté et récompenses ajustées selon le niveau du joueur :
  $$\text{Quantité} = \text{base-amount} \times \text{level}^2$$
  $$\text{Gains} = \text{base-money} \times \text{level}^2$$
* 🏆 **Level-Up Statique** : Validation par un nombre fixe et configurable de quêtes complétées dans la catégorie (ex: `completions-to-level-up: 5`).
* 🔑 **Quotas Journaliers par Permissions** : Définition des limites d'acceptation selon les grades (`default: 1`, `vip: 5`, `admin: -1`).
* 📱 **GUIs Dédiés** : 3 menus YAML indépendants (`personal.yml`, `personal_categories.yml`, `personal_progression.yml`).

### 🌍 4. Quêtes Communautaires (Événements Globaux)
* 📣 **Déclenchement Admin** : Commande `/q community start <id> <durée> [objectif]` pour lancer des événements temporaires à l'échelle du serveur.
* 💰 **Contributions Collectives** : Les joueurs coopèrent pour atteindre une cagnotte globale (Lumens / Vault ou items).
* 🏆 **Classement en Temps Réel** : Suivi des meilleurs contributeurs (Top 1, Top 2, Top 3) et récompenses par paliers de participation.
* 🤖 **Intégration Discord Webhook** : Envoi automatique d'embeds illustrés lors du lancement, du suivi et de la clôture avec le classement final.
* 📜 **Fichiers de Logs** : Enregistrement de rapports détaillés sous `logs/community_events/`.

### 📊 5. Moteur de Suivi Unifié (HUD Multi-Canal)
* 📺 **Canaux d'Affichage** : Les joueurs choisissent leur mode de suivi préféré via la commande `/q track <mode>` ou dans les GUIs :
  * `BOSS_BAR` (Adventure BossBar native Paper)
  * `ACTION_BAR` (Message d'Actionbar éphémère)
  * `CHAT` (Notifications dans le tchat)
  * `NONE` (Désactivé)
* 🔕 **Masquage par Catégorie** : Possibilité pour les joueurs de désactiver le suivi d'une catégorie spécifique sans désactiver le tracker global.

---

## 🎨 Zero Hardcoding & Charte Graphique

Tout l'affichage du plugin est **100% configurable** via les fichiers YAML :
* Prise en charge des **CustomModelData** (`custom_model_data: 1001`) sur tous les items pour les textures de resource packs personnalisées.
* Support complet du format **MiniMessage** et des codes couleurs Bukkit dans tous les fichiers de langue (`lang/`) et de menus (`menus/`).

---

## 🛠️ Prérequis & Dépendances

| Dépendance | Requis / Optionnel | Rôle |
| :--- | :--- | :--- |
| **Paper 1.21.8+** | **Obligatoire** | Serveur Minecraft cible |
| **Java 21** | **Obligatoire** | Environnement d'exécution |
| **nightcore** (2.9.3+) | **Obligatoire** | Framework de base (menus, configs, commandes) |
| **SuperiorSkyblock2** | Optionnel | Requis pour les Quêtes d'Îles coopératives |
| **Citizens** / **FancyNPCs** | Optionnel | Requis pour les objectifs d'interaction PNJ |
| **WorldGuard** | Optionnel | Requis pour les objectifs de détection de région |
| **Vault** / **DanaEconomy** | Optionnel | Requis pour les dépôts d'économie (Lumens) |

---

## ⚙️ Compilation

Pour compiler le projet depuis les sources :

```bash
git clone https://github.com/DanaKube-Mc/DanaQuests.git
cd DanaQuests
mvn clean package
```

Le fichier JAR compilé sera généré dans le dossier `target/`.
