package model;

import exception.WorkoutAppException;
import java.io.Serializable;

public class User implements Serializable {

    private static final long serialVersionUID = 1;

    private String name;
    private int age;
    private double weightKg;
    private double heightCm;

    public User() {
        this.name = "";
    }

    public User(String name, int age, double weightKg, double heightCm) throws WorkoutAppException {
        if (name == null || name.trim().isEmpty()) {
            throw new WorkoutAppException("Name cannot be blank.");
        }
        if (age <= 0) {
            throw new WorkoutAppException("Age must be positive.");
        }
        if (weightKg <= 0) {
            throw new WorkoutAppException("Weight must be positive.");
        }
        if (heightCm <= 0) {
            throw new WorkoutAppException("Height must be positive.");
        }
        this.name = name;
        this.age = age;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
    }

    /** Returns Body Mass Index: weight(kg) / height(m)^2 */
    public double getBMI() {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    // ---------- Getters ----------

    public String getName() { return name; }
    public int getAge() { return age; }
    public double getWeightKg() { return weightKg; }
    public double getHeightCm() { return heightCm; }

    // ---------- Setters ----------

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    @Override
    public String toString() {
        return String.format("User{name='%s', age=%d, weight=%.1f kg, height=%.1f cm, BMI=%.1f}",
                name, age, weightKg, heightCm, getBMI());
    }
}
