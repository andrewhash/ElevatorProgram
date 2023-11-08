import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

// Define a Passenger class to represent elevator passengers.
class Passenger {
    private int arrivalTick;
    private int destinationFloor;

    // Constructor for creating a passenger with arrival time and destination floor.
    public Passenger(int arrivalTick, int destinationFloor) {
        this.arrivalTick = arrivalTick;
        this.destinationFloor = destinationFloor;
    }

    // Getter method for getting the arrival time of the passenger.
    public int getArrivalTick() {
        return arrivalTick;
    }

    // Getter method for getting the destination floor of the passenger.
    public int getDestinationFloor() {
        return destinationFloor;
    }
}

// Define an Elevator class to represent an elevator's properties and actions.
class Elevator {
    private int currentFloor;
    private int direction;
    private int capacity;
    private List<Passenger> passengers;

    // Constructor for creating an elevator with a specified capacity.
    public Elevator(int capacity) {
        this.currentFloor = 1;
        this.direction = 1;
        this.capacity = capacity;
        this.passengers = new ArrayList<>();
    }

    // Getter method for getting the current floor of the elevator.
    public int getCurrentFloor() {
        return currentFloor;
    }

    // Getter method for getting the direction in which the elevator is moving.
    public int getDirection() {
        return direction;
    }

    // Getter method for getting the capacity of the elevator.
    public int getCapacity() {
        return capacity;
    }

    // Getter method for getting the list of passengers inside the elevator.
    public List<Passenger> getPassengers() {
        return passengers;
    }

    // Method to move the elevator by changing its current floor.
    public void move() {
        currentFloor += direction;
    }

    // Method to load a passenger into the elevator.
    public void loadPassenger(Passenger passenger) {
        passengers.add(passenger);
    }

    // Method to unload passengers at a specific floor.
    public void unloadPassengers(int floor) {
        passengers.removeIf(passenger -> passenger.getDestinationFloor() == floor);
    }
}

// Define a Building class to represent a building with waiting passengers.
class Building {
    private int floors;
    private double passengersProbability;
    private List<Passenger> waitingPassengers;

    // Constructor for creating a building with a specified number of floors and passenger probability.
    public Building(int floors, double passengersProbability) {
        this.floors = floors;
        this.passengersProbability = passengersProbability;
        this.waitingPassengers = new ArrayList<>();
    }

    // Getter method for getting the number of floors in the building.
    public int getFloors() {
        return floors;
    }

    // Getter method for getting the passenger probability in the building.
    public double getPassengersProbability() {
        return passengersProbability;
    }

    // Getter method for getting the list of waiting passengers in the building.
    public List<Passenger> getWaitingPassengers() {
        return waitingPassengers;
    }

    // Method to generate passengers at a given time tick based on the passenger probability.
    public void generatePassengers(int tick) {
        Random random = new Random();
        for (int floor = 1; floor <= floors; floor++) {
            if (random.nextDouble() < passengersProbability) {
                int destinationFloor = floor;
                while (destinationFloor == floor) {
                    destinationFloor = random.nextInt(floors) + 1;
                }
                waitingPassengers.add(new Passenger(tick, destinationFloor));
            }
        }
    }
}

// Define an ElevatorSimulation class to run the elevator simulation.
class ElevatorSimulation {
    private String structures;
    private int floors;
    private double passengers;
    private int elevators;
    private int elevatorCapacity;
    private int duration;

    // Method to load simulation properties from a file.
    public void loadProperties(String filePath) {
        Properties properties = new Properties();
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
            if (inputStream != null) {
                properties.load(inputStream);
                System.out.println("Configuration loaded successfully.");
            } else {
                throw new FileNotFoundException("Property file '" + filePath + "' not found in the classpath");
            }
        } catch (IOException e) {
            // File not found or unable to load, use default values
            properties.setProperty("structures", "linked");
            properties.setProperty("floors", "32");
            properties.setProperty("passengers", "0.03");
            properties.setProperty("elevators", "1");
            properties.setProperty("elevatorCapacity", "10");
            properties.setProperty("duration", "500");
            System.out.println("Using default configuration.");
        }

        structures = properties.getProperty("structures");
        floors = Integer.parseInt(properties.getProperty("floors"));
        passengers = Double.parseDouble(properties.getProperty("passengers"));
        elevators = Integer.parseInt(properties.getProperty("elevators"));
        elevatorCapacity = Integer.parseInt(properties.getProperty("elevatorCapacity"));
        duration = Integer.parseInt(properties.getProperty("duration"));
    }

    // Method to run the elevator simulation.
    public void runSimulation() {
        Building building = new Building(floors, passengers);
        List<Elevator> elevatorsList = new ArrayList<>();

        for (int i = 0; i < elevators; i++) {
            elevatorsList.add(new Elevator(elevatorCapacity));
        }

        int tick = 0;
        int totalPassengers = 0;
        int longestTime = 0;
        int shortestTime = Integer.MAX_VALUE;
        int totalConveyanceTime = 0;

        while (tick < duration) {
            building.generatePassengers(tick);

            for (Elevator elevator : elevatorsList) {
                int currentFloor = elevator.getCurrentFloor();
                int direction = elevator.getDirection();

                // Unload passengers
                elevator.unloadPassengers(currentFloor);

                // Load passengers
                List<Passenger> waitingPassengers = building.getWaitingPassengers();
                Iterator<Passenger> iterator = waitingPassengers.iterator();
                while (iterator.hasNext()) {
                    Passenger passenger = iterator.next();
                    if (passenger.getArrivalTick() <= tick && passenger.getDestinationFloor() * direction > currentFloor * direction) {
                        if (elevator.getPassengers().size() < elevator.getCapacity()) {
                            iterator.remove();
                            elevator.loadPassenger(passenger);
                        }
                    }
                }

                // Move elevator
                elevator.move();
            }

            // Calculate statistics
            for (Elevator elevator : elevatorsList) {
                List<Passenger> passengers = elevator.getPassengers();
                for (Passenger passenger : passengers) {
                    int conveyanceTime = tick - passenger.getArrivalTick();
                    totalConveyanceTime += conveyanceTime;
                    longestTime = Math.max(longestTime, conveyanceTime);
                    shortestTime = Math.min(shortestTime, conveyanceTime);
                }
                totalPassengers += passengers.size();
            }

            tick++;
        }

        double averageConveyanceTime = (double) totalConveyanceTime / totalPassengers;

        System.out.println("Average length of time between passenger arrival and conveyance to the final destination: " + averageConveyanceTime);
        System.out.println("Longest time between passenger arrival and conveyance to the final destination: " + longestTime);
        System.out.println("Shortest time between passenger arrival and conveyance to the final destination: " + shortestTime);
    }

    // Main method to run the elevator simulation.
    public static void main(String[] args) {
        ElevatorSimulation simulation = new ElevatorSimulation();
        simulation.loadProperties("elevator.properties");
        simulation.runSimulation();
    }
}
