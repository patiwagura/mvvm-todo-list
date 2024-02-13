# mvvm-todo-list App

_A simple todo-list-app built with **android architecture-components** & **mvvm**._

## App Screenshots:
![todo app screenshot showing the features](/screenshots/todo-list-1.png)

## Features:
- search todos (filter list items)
- sort list items (by: name & date_created).
- mark completed items (checked items & strike-through)
- ! mark important items (! items with priority appear on top of list).
- show/hide completed todos.
- Add, Update
- Delete items (Swipe to delete & undo snackBar)


## Libraries, tools & architecture used:
- 100% Kotlin.
- Room (SQLite).
- Jetpack DataStore :- store key/value pairs (small pieces of data e.g settings & preferences).
- Flow (Coroutines).
- Navigation Component.
- Dependency injection (Dagger Hilt).
- View Binding.
- MVVM (Model-View-ViewModel) - structure app into components (separation of concerns).
- ViewModel + LiveData


### Advantages of a good app Architecture:
- separation of concerns, every component has its own/single responsibility.
- easier to maintain & add more features to the app.

