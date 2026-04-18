# 📝 Notes App

A modern Android Notes Application built using **Kotlin and Jetpack Compose**, focused on clean architecture, responsive UI, and efficient local data storage.

---

## 🚀 Features

* 🧱 Grid-based notes layout
* 📌 Pin / Unpin notes
* 🗑️ Swipe to delete with undo option
* ⚠️ Delete confirmation dialog
* 🔍 Search notes (title & content)
* 🖼️ Multiple images support per note
* 📝 Add & edit notes using bottom sheet UI
* 🌙 Dark theme (default)
* ⚡ Smooth and responsive UI

---

## 🏗️ Tech Stack

| Layer         | Technology               |
| ------------- | ------------------------ |
| Language      | Kotlin                   |
| UI            | Jetpack Compose          |
| Architecture  | MVVM                     |
| Database      | Room Database            |
| Async         | Kotlin Coroutines + Flow |
| Image Loading | Coil                     |
| Design        | Material 3               |

---

## 📂 Project Structure

```
com.example.notesapp │ ├── data/ │ ├── Note.kt │ ├── NoteDao.kt │ ├── NoteDatabase.kt │ └── Converters.kt │ ├── repository/ │ └── NoteRepository.kt │ ├── viewmodel/ │ ├── NoteViewModel.kt │ └── NoteViewModelFactory.kt │ ├── ui/ │ └── screens/ │ └── NotesScreen.kt │ └── MainActivity.kt
```

---

## ⚙️ Installation

1. Clone the repository:

```
git clone https://github.com/dev-afrith/notes-app.git
```

2. Open in Android Studio

3. Build & Run ▶️

---


## 🎯 Learning Outcomes

* Building modern UI with Jetpack Compose
* Implementing MVVM architecture
* Managing local data with Room
* Handling state using StateFlow
* Creating scalable Android applications

---

## 👨‍💻 Author

**Muhammad Afrith D**

* 📧 Email: [afrithafrith1507@gmail.com](mailto:afrithafrith1507@gmail.com)
* 🔗 GitHub: https://github.com/dev-afrith

---

## ⭐ Support

If you found this project useful, consider giving it a ⭐ on GitHub.
