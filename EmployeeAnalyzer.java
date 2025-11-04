import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class EmployeeAnalyzer {

    static class Employee {
        private String id;
        private String firstName;
        private String lastName;
        private double salary;
        private String managerId;
        private List<Employee> subordinates;

        public Employee(String id, String firstName, String lastName, double salary, String managerId) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.salary = salary;
            this.managerId = managerId;
            this.subordinates = new ArrayList<>();
        }

        public String getFullName() {
            return firstName + " " + lastName;
        }

        public void addSubordinate(Employee e) {
            subordinates.add(e);
        }

        public List<Employee> getSubordinates() {
            return subordinates;
        }

        public double getSalary() {
            return salary;
        }

        public String getManagerId() {
            return managerId;
        }
    }

    public static void main(String[] args) {
        try {
            Map<String, Employee> employees = readEmployees("employees.csv");
            buildSubordinateRelationships(employees);
            analyzeSalaries(employees);
            analyzeReportingChains(employees);
        } catch (Exception e) {
            System.err.println("Error processing employee data: " + e.getMessage());
        }
    }

    private static Map<String, Employee> readEmployees(String filePath) throws IOException {
        Map<String, Employee> employees = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                String id = parts[0].trim();
                String firstName = parts[1].trim();
                String lastName = parts[2].trim();
                double salary = Double.parseDouble(parts[3].trim());
                String managerId = parts.length > 4 && !parts[4].trim().isEmpty() ? parts[4].trim() : null;

                Employee emp = new Employee(id, firstName, lastName, salary, managerId);
                employees.put(id, emp);
            }
        }
        return employees;
    }

    private static void buildSubordinateRelationships(Map<String, Employee> employees) {
        for (Employee emp : employees.values()) {
            String managerId = emp.getManagerId();
            if (managerId != null) {
                Employee manager = employees.get(managerId);
                if (manager != null) {
                    manager.addSubordinate(emp);
                }
            }
        }
    }

    private static void analyzeSalaries(Map<String, Employee> employees) {
        for (Employee manager : employees.values()) {
            List<Employee> subs = manager.getSubordinates();
            if (!subs.isEmpty()) {
                double avgSubSalary = subs.stream().mapToDouble(Employee::getSalary).average().orElse(0);
                double diff = manager.getSalary() - avgSubSalary;
                if (diff < 20) {
                    System.out.printf("Manager %s earns less than required by %.2f%n", manager.getFullName(), 20 - diff);
                } else if (diff > 50) {
                    System.out.printf("Manager %s earns more than allowed by %.2f%n", manager.getFullName(), diff - 50);
                }
            }
        }
    }

    private static void analyzeReportingChains(Map<String, Employee> employees) {
        for (Employee emp : employees.values()) {
            int count = 0;
            Employee current = emp;
            while (current.getManagerId() != null) {
                current = employees.get(current.getManagerId());
                if (current == null) break;
                count++;
            }
            if (count > 4) {
                System.out.printf("Employee %s has a reporting chain longer by %d%n", emp.getFullName(), count - 4);
            }
        }
    }
}
