package model;

/**
 * Represents the app user whose workouts are being tracked.
 */
public class User {

    private String name;
    private int age;
    private double weightKg;
    private double heightCm;

    public User(String name, int age, double weightKg, double heightCm) {
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
