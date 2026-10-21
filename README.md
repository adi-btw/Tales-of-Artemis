# Tales of Artemis

A Java desktop typing adventure game where players defeat enemies by typing words, progress through levels, and earn scores.

Built as a team project for **CS2212B: Software Engineering II at Western University**, Tales of Artemis combines interactive gameplay with persistent player accounts, leaderboards, and parental controls.

## Features

- **Typing-based combat:** Type words to attack enemies and progress through the game.
- **Level progression:** Advance through levels with time pressure and gameplay challenges.
- **Power-ups:** Use gameplay effects to support your progress.
- **Player accounts:** Register, log in, and maintain a player profile.
- **Persistent progress:** Save player information between sessions.
- **Leaderboards:** Record scores and view high-score rankings.
- **Parental controls:** Manage gameplay access through dedicated controls.
- **Tutorial and player hub:** Learn the mechanics and navigate between game features.

## Technology Stack

| Area | Technologies |
|---|---|
| Language | Java |
| Desktop interface | Swing |
| Build and dependencies | Maven |
| Data persistence | JSON with Gson |
| Automated testing | JUnit 5 |
| Architecture | Model–View–Controller (MVC) |

## My Contribution

**I was responsible for all backend development.**

My work covered the application logic and supporting systems behind the game's interface, including:

- Player registration, login, and account management.
- Player profiles and persistent progress.
- Score tracking and leaderboard persistence.
- Game-session and level-progression logic.
- Typing mechanics and gameplay-state management.
- Backend logic supporting parental controls.
- JSON-based data storage using Gson.
- Unit testing for typing, account-management, and level logic.
- Integration and system testing, defect tracking, and technical documentation.

I also contributed use cases, activity diagrams, a UML class diagram, and Javadoc documentation to explain the system's behavior and structure.

The project was developed collaboratively. My responsibility was the backend; the complete application also includes interface work and other contributions from my teammates.

## Architecture

Tales of Artemis follows an MVC structure to separate gameplay and application logic from the Swing interface.

- **Model:** Represents players, enemies, power-ups, levels, profiles, and scores.
- **View:** Presents menus, gameplay, tutorials, leaderboards, and account screens.
- **Control and application logic:** Coordinates typing input, game sessions, account operations, and progression.

This separation supports testing application behavior independently of the interface.

## Getting Started

### Prerequisites

- JDK 23
- Maven 3.9 or newer
- A desktop environment capable of running Java Swing applications

### Clone the Repository

```bash
git clone https://github.com/adi-btw/Tales-of-Artemis.git
cd Tales-of-Artemis
```

### Open the Project

1. Open the folder containing `pom.xml` in IntelliJ IDEA or another Java IDE.
2. Import the project as a Maven project.
3. Configure the project SDK to JDK 23.
4. Allow Maven to download the dependencies.
5. Find the application entry point containing `public static void main(String[] args)` and run it.

### Build and Test

Run these commands from the folder containing `pom.xml`:

```bash
mvn clean compile
mvn test
```

## Testing

Testing covered application behavior at several levels:

- **Unit testing:** Typing behavior, account management, and level logic.
- **Integration testing:** Interactions between connected application components.
- **Validation testing:** Checking behavior against project requirements.
- **System testing:** Complete user flows through the application.

Test scenarios included registration and login, typing feedback, score updates, level progression, pause behavior, game completion, leaderboards, progress persistence, and parental controls.

## What I Learned

This project strengthened my experience with:

- Object-oriented design and MVC architecture.
- Separating backend logic from a desktop interface.
- Implementing persistent accounts and application data.
- Developing automated tests for application logic.
- Investigating defects across connected components.
- Communicating design decisions through diagrams and documentation.
- Integrating backend systems into a collaboratively developed application.

## Project Background

Developed by **Group 25** for **CS2212B: Software Engineering II** at Western University.

This repository presents the team's project as part of my software development portfolio. The contribution section describes my individual responsibilities.
