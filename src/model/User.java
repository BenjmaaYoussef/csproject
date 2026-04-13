package model;

/**
 * Represents the app user's profile.
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

    /**
     * Calculates the user's Body Mass Index (BMI).
     * BMI = weight(kg) / (height(m))^2
     */
    public double getBMI() {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /**
     * Returns a BMI category label based on WHO standards.
     */
    public String getBMICategory() {
        double bmi = getBMI();
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    // ---------- Getters / Setters ----------

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public double getHeightCm() { return heightCm; }
    public void setHeightCm(double heightCm) { this.heightCm = heightCm; }

    @Override
    public String toString() {
        return String.format(
            "User: %s | Age: %d | Weight: %.1f kg | Height: %.1f cm | BMI: %.2f (%s)",
            name, age, weightKg, heightCm, getBMI(), getBMICategory()
        );
    }
}
