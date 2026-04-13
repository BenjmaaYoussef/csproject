# Workout App – UML Class Diagram

```
┌──────────────────────────────────────┐
│             <<enum>>                  │
│           ExerciseType                │
├──────────────────────────────────────┤
│  STRENGTH                             │
│  CARDIO                               │
│  FLEXIBILITY                          │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│              Exercise                 │
├──────────────────────────────────────┤
│ - name       : String                 │
│ - sets       : int                    │
│ - reps       : int                    │
│ - weightKg   : double                 │
│ - durationMin: int                    │
│ - type       : ExerciseType           │
├──────────────────────────────────────┤
│ + Exercise(name, sets, reps,          │
│            weightKg, durationMin,     │
│            type)                      │
│ + getters / setters                   │
│ + toString() : String                 │
│ + validate() : void  [throws]         │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│           WorkoutSession              │
├──────────────────────────────────────┤
│ - date      : String (yyyy-MM-dd)     │
│ - exercises : ArrayList<Exercise>     │
│ - notes     : String                  │
├──────────────────────────────────────┤
│ + WorkoutSession(date, notes)         │
│ + addExercise(e: Exercise): void      │
│   [throws DuplicateExerciseException] │
│ + removeExercise(name: String): void  │
│ + getExercises(): ArrayList<Exercise> │
│ + toString(): String                  │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│                User                   │
├──────────────────────────────────────┤
│ - name      : String                  │
│ - age       : int                     │
│ - weightKg  : double                  │
│ - heightCm  : double                  │
├──────────────────────────────────────┤
│ + User(name, age, weightKg,heightCm) │
│ + getBMI(): double                    │
│ + getters / setters                   │
│ + toString(): String                  │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│           WorkoutManager              │
├──────────────────────────────────────┤
│ - sessions  : ArrayList<WorkoutSession>│
│ - user      : User                    │
├──────────────────────────────────────┤
│ + addSession(s: WorkoutSession): void │
│ + getSessionByDate(date): Session     │
│   [throws WorkoutNotFoundException]   │
│ + getAllSessions(): ArrayList<...>    │
│ + getSummary(): String                │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│    <<Exception Hierarchy>>            │
├──────────────────────────────────────┤
│  Exception                            │
│    └── WorkoutAppException            │
│           ├── InvalidExerciseException│
│           ├── WorkoutNotFoundException│
│           └── DuplicateExerciseExc.   │
└──────────────────────────────────────┘

Relationships:
  WorkoutSession  *──────  Exercise       (composition: session has many exercises)
  WorkoutManager  1──────  User           (association: manager tracks one user)
  WorkoutManager  1──────* WorkoutSession (aggregation: manager holds sessions)
  Exercise        ──────── ExerciseType   (uses enum)

Main.java:
  Creates WorkoutManager, drives the console menu loop.
```
