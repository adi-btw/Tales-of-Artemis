# Tales of Artemis

## Description
Tales of Artemis is a typing-based educational game where players defeat enemies by correctly typing displayed words. The game incorporates real-time gameplay mechanics, user account management, performance tracking, and level progression. As players advance, difficulty increases through faster gameplay, longer words, and more complex interactions. The system also supports multiple user accounts, persistent data storage, and parental controls.

---

## Required Libraries and Tools

The following tools are required to build and run the software:

- Java Development Kit (JDK) 23 or newer
- Apache Maven 3.9.0 or higher
- Operating System: Windows 10 or Windows 11

Optional (recommended):
- VS Code with Java Extension Pack

---

## Dependencies

The project uses the following third-party libraries:

- Gson 2.11.0 (for JSON data storage and parsing)
- JUnit 5.11.0 (for unit testing)

---

## Installation of Required Tools

### 1. Install Java (JDK 23+)
1. Download from: https://adoptium.net/
2. Install using default settings
3. Verify installation:

```
java -version
```

---

### 2. Install Apache Maven (3.9+)
1. Download from: https://maven.apache.org/download.cgi
2. Extract to:

```
C:\Program Files\apache-maven-3.9.x
```

3. Add to PATH:

```
C:\Program Files\apache-maven-3.9.x\bin
```

4. Verify installation:

```
mvn -v
```

---

## Project Structure

```
src/main/java   → main application source code
src/test/java   → unit tests
data/           → JSON data files (players.json, leaderboard.json)
src/main/resources → game assets (images, audio, word list)
target/         → compiled files and generated documentation
```

---

## Building the Software (from source)

1. Open Command Prompt or PowerShell
2. Navigate to the project folder:

```
cd path\to\group25
```

3. Run:

```
mvn clean install
```

This will:
- Compile the project
- Resolve dependencies
- Run all unit tests
- Build the executable JAR files

---

## Running the Software

**Option 1 — Maven exec (recommended):**

```
mvn exec:java
```

**Option 2 — Run the compiled fat JAR (after `mvn package`):**

```
java -jar target/group25-1.0-SNAPSHOT-runnable.jar
```

**Option 3 — IDE (VS Code, IntelliJ, etc.):**
- Open the project
- Run `Main.java`

---

## Running Tests

To execute all unit tests:

```
mvn test
```

Test files are located in:

```
src/test/java
```

---

## Generating API Documentation

To generate HTML Javadoc documentation:

```
mvn javadoc:javadoc
```

The generated documentation can be found at:

```
target/site/apidocs/index.html
```

Open this file in a browser to view the full API documentation.

---

## User Guide

### Main Menu
- Start Game: Begins gameplay (guest mode — no stats saved)
- Login/Register: Access user accounts
- High Scores: View leaderboard
- Tutorial: Learn controls
- Parental Control: Manage stats and users
- Exit: Close application

---

### Gameplay
- Words appear on enemies
- Type the word correctly to defeat the enemy
- Use arrow keys to move and collect powerups
- Incorrect typing flashes the screen red
- Player health decreases if enemies reach the player
- Score increases with each word typed correctly
- Press ESC to pause

---

### Controls
- Keyboard: Used for typing words
- Left/Right Arrow Keys: Move player
- ESC Key: Pause the game
- Mouse: Navigation between menus

---

### Levels
- 3 levels with increasing difficulty
- Easy: short words, slower enemies
- Medium: medium words, moderate enemy speed
- Hard: long words, fast enemies

---

### Powerups
- Health Powerup (green egg): Restores player health
- Slow Time Powerup: Slows all enemies for 5 seconds
- Powerups spawn randomly on enemy defeat

---

### Accounts
- Users can register and log in
- Player data is saved locally in `data/players.json`
- Tracks:
  - High score
  - Accuracy
  - Words per minute (WPM)
  - Level progression
  - Total play time

---

## Test Accounts

| Username | Password | Notes |
|----------|----------|-------|
| test7    | 123!     | Has existing stats and high score |
| test1    | (see below) | Admin account |

Additional test accounts: test2, test3, test4, test6 (all registered, no stats).

Users may also create their own accounts via the Register screen.

---

## Parental Controls

Parental controls allow restricted access to administrative features such as:
- Viewing player statistics
- Resetting player data
- Managing accounts (create/delete)
- Resetting the leaderboard

### Access Instructions:
1. Navigate to **Parental Controls** from the main menu
2. Enter the password: `Parent123!`

---

## Additional Notes

- All data is stored locally using JSON files — no internet connection is required
- The system is designed for desktop use on Windows
- Ensure Java 23 and Maven are correctly installed before building
- If running from an IDE, ensure the working directory is set to the project root

---

## For TA Marking

- All features described in the Requirements and Design documents are implemented
- Unit tests are included in `src/test/java` (run with `mvn test`)
- Javadoc documentation can be generated with `mvn javadoc:javadoc`
- Example save data is pre-loaded in `data/players.json` and `data/leaderboard.json`
- Test account with real gameplay stats: username `test7`, password `123!`
- The application has been tested on Windows 10/11 environments

---
