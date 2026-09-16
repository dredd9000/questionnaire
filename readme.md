# Telegram Survey Management System 📊

> A real-time survey management platform featuring a **Telegram Bot** for community participation and a **Java Swing GUI** for live monitoring, automated AI question generation, and analytics.

---

## 🌟 Highlights & UX Features

* **Real-Time Swing Dashboard:** Live-updating tables and counters for community members and active survey responses—no manual refreshes required.
* **ChatGPT AI Integration:** Option to manually compose survey questions or generate contextual multi-question surveys automatically via OpenAI's ChatGPT API.
* **Granular Survey Tracking:** Tracks individual participant progress (`0/3`, `1/3`, `3/3`) without mutating global user profiles.
* **Smart Reminders & Timer:** Automated live countdowns, delayed publishing, auto-closure (at 5 minutes or 100% completion), and targeted 3-minute completion reminders.
* **Visual Analytics:** Sorted, percentage-based result displays grouped by answer frequency once a survey closes.

---

## 🛠️ Architecture Overview

The system consists of two tightly coupled components:

```
┌─────────────────────────┐               ┌─────────────────────────┐
│     Java Swing GUI      │               │      Telegram Bot       │
├─────────────────────────┤               ├─────────────────────────┤
│ • Real-time Monitoring  │ ◄───────────► │ • User Onboarding       │
│ • Survey Generator      │  Event Bus /  │ • Question Dispatching  │
│ • ChatGPT API Client    │  Data Layer   │ • Answer Collection     │
│ • Results Analytics     │               │ • Target Reminders      │
└─────────────────────────┘               └─────────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites

* **Java Development Kit (JDK):** Version 17 or higher recommended.
* **Maven** (or Gradle) for dependency management.
* **Telegram Bot Token:** Obtained via [@BotFather](https://t.me/BotFather).
* **OpenAI API Key:** Obtained via [Shai](https://front-2025.onrender.com/).

### Configuration

Create or update the configuration file located at:

```text
src/main/resources/config.properties
```

Populate it with your actual credentials:

```properties
telegram.bot.token=YOUR_TELEGRAM_BOT_TOKEN_HERE
chatgpt.token=YOUR_OPENAI_API_KEY_HERE
chatgpt.url=https://shaitest-production-3066.up.railway.app/api-request
```

> ⚠️ **Security Note:** I Never commit `config.properties`, Please add this file so the project will work

---

## 📋 Features Breakdown

### 1. Community Management
* **Onboarding:** Users join the community by sending `/start`, `Hi`, or `היי` to the Telegram bot.
* **Global Roster:** Displays total member count alongside names, Telegram handles, and join timestamps.
* **Broadcast Alerts:** Every member receives a notification when a new user joins the community.

### 2. Survey Creation & Dispatch
* **Question Constraints:** Supports 1–3 questions per survey, with 2–4 answer choices each.
* **Dual Creation Modes:**
  * **Manual:** Custom questions and choices.
  * **AI-Assisted:** Provide a topic and inspect/edit ChatGPT-generated content before publishing.
* **Scheduling:** Immediate broadcast or delayed release with a live UI countdown timer.
* **Minimum Threshold:** Requires a minimum of **3 community members** to launch.

### 3. Active Survey Execution & Real-Time UX
* **Live Progress Bar / Table:** View individual participant statuses (`Not Started`, `In Progress`, `Completed`).
* **Active Counters:** Live metrics for total participants, completed responses, remaining participants, and time left.
* **Automated Logic:**
  * **3-Minute Mark:** Sends a one-time reminder **only** to users with incomplete surveys.
  * **5-Minute Mark:** Closes the survey automatically and stops accepting answers.
  * **Early Closure:** Closes instantly if 100% of participants finish before the time limit.

### 4. Results & Analytics
* Post-survey results panel presenting answer distributions.
* Automatic sorting by frequency (highest voted options displayed first).
* Displays both vote counts and percentage ratios per option.

---

## 📁 5. Project structure

```text
telegram-survey-system/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/questionnaire/
│   │   │       ├── core/          # All the logic, bot etc.
│   │   │       ├── UI/            # Java Swing frames, models, and custom renders
│   │   │       ├── Globals.java   # Some global objects, like custom toast 
│   │   │       ├── Main.java      # The main function that starts the program
│   │   │       └── Utils.java     # Util methods
│   │   └── resources/
│   │       └── config.properties  # Secrets and endpoints
└── README.md
```