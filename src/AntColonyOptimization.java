import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.IntStream;

class AntColonyOptimization {

    private float alpha = 1;
    private double beta = 5;
    int maxIterations = 5;
    private int numberOfCities;
    private float solutionscore;
    double evaporation = 0.5;

    int Q = 500;
    private Vector<Vector<Integer>> graph;
    private Vector<Integer> weights;
    private Vector<Double> trails;
    private Vector<Integer> counter_edges;
    private Vector<Integer> Neighborhood;
    private List<Ant> ants = new ArrayList<>();
    private Random random = new Random();
    private Vector<Double> probabilities;
    private int vertex;

    private int bestTourOrder;
    private float bestTourLength;
    /**
     * Constructor
     */
    AntColonyOptimization(double itertions, int number_of_ants) throws FileNotFoundException {
        load();
        IntStream.range(0, 5)
                .forEach(i -> ants.add(new Ant()));
        evaporation = itertions;
        Q = number_of_ants;
    }

    /**
     * Use this method to run the main logic
     */
    void solve() {

        clearTrails();

        for (int i = 0; i < maxIterations; i++) {
            moveAnts();
            updateTrails();
        }

        System.out.println("Best tour length: " + (bestTourLength));
        System.out.println("Best tour order: " + bestTourOrder);
    }

    /**
     * At each iteration, move ants
     */
    private void moveAnts() {
            for (Ant ant : ants) {
                solutionscore =0;
                ant.Solution = new Vector<>();
                vertex = random.nextInt(numberOfCities);
                Vector<Integer> used_vertexes = new Vector<>();
                while(used_vertexes.size()<numberOfCities) {
                    int index = selectNextCity(ant);
                    ant.visitCity(index);
                    counter_edges(index);
                    count(index);
                    updateActual(index);
                    if(!used_vertexes.contains(vertex))
                        used_vertexes.add(vertex);
                }
                updateBest(ant);
            }


    }

    /**
     * Add Neighborhood
     */
    private void getNeighborhood(){
        Neighborhood = new Vector<>();
        IntStream.range(0, graph.size()).filter(i -> (
                (graph.get(i).get(0) == vertex || graph.get(i).get(1) == vertex))).forEach(i -> Neighborhood.add(i));
    }
    /**
     * Select next city for each ant
     */
    private int selectNextCity(Ant ant) {
        getNeighborhood();
        boolean all_used = true;
        for (Integer neighborhood : Neighborhood) {
            if (!ant.Solution.contains(neighborhood)) {
                all_used = false;
            }
        }
        if(all_used) {
            Vector<Integer> last_time_used_edge = new Vector<>();
            int oldest_edge;
            int index;
            for (int j = 0; j < Neighborhood.size(); j++) {
                last_time_used_edge.add(0);
            }

            for (int i = 0; i<ant.Solution.size(); i++) {
                for(int j =0;j<Neighborhood.size();j++) {
                    if (Neighborhood.get(j).equals(ant.Solution.get(i))) {
                        last_time_used_edge.set(j, i);
                    }
                }
            }

            oldest_edge = last_time_used_edge.get(0);
            index = 0;
            for (int j = 0; j < last_time_used_edge.size(); j++) {
                if (oldest_edge > last_time_used_edge.get(j)) {
                    oldest_edge = last_time_used_edge.get(j);
                    index = j;
                }
            }
            return Neighborhood.get(index);
        }else {
            Vector<Integer>not_all_used = new Vector<>();
            for (Integer neighborhoods : Neighborhood) {
                if (!ant.Solution.contains(neighborhoods)) {
                    not_all_used.add(neighborhoods);
                }
            }
            calculateProbabilities(not_all_used);
            double r = random.nextDouble();
            double total = 0;
            for (int i=0;i<not_all_used.size();i++) {
                    total += probabilities.get(i);
                    if (total >= r) {
                        return not_all_used.get(i);
                    }
            }
        }
        throw new RuntimeException("No other cities");
    }

    /**
     * Calculate the next city picks probabilites
     */
    private void calculateProbabilities(Vector<Integer> not_all_used) {
        double pheromone = 0.0;
        probabilities = new Vector<>();
        for (Integer next : not_all_used) {
                pheromone += Math.pow(trails.get(next), alpha) * Math.pow(1.0 /weights.get(next), beta);
        }
        for (Integer next : not_all_used) {
                double numerator = Math.pow(trails.get(next), alpha) * Math.pow(1.0 / weights.get(next), beta);
                probabilities.add(numerator / pheromone);
        }
    }

    /**
     * Update trails that ants used
     */
    private void updateTrails() {
        IntStream.range(0, graph.size()).forEach(i -> {
            trails.set(i, trails.get(i) * evaporation);
        });
        for (Ant a : ants) {
            double contribution = Q / a.trailLength(weights);
            for (int i = 0; i < a.Solution.size(); i++) {
                trails.set(a.Solution.get(i), +contribution) ;
            }
        }
    }
    /**
     * Read graph and weights from file
     */
    private void load() throws FileNotFoundException {

        String file = "4.txt";
        int number;
        Scanner in = new Scanner(new File(file));

        graph = new Vector<>();
        weights = new Vector<>();
        while (in.hasNext()) {
            Vector<Integer> edge = new Vector<>();
            number = in.nextInt();
            if(number>numberOfCities)
                numberOfCities=number;
            edge.add(number);
            number = in.nextInt();
            if(number>numberOfCities)
                numberOfCities=number;
            edge.add(number);
            graph.add(edge);
            weights.add(in.nextInt());

        }
        counter_edges = new Vector<>();
        for (int i = 0; i < graph.size(); i++) {
            counter_edges.add(0);
        }
    }
    /**
     * Upadte actual vertex
     */
    private void updateActual(int index){
        if (graph.get(index).get(0) == vertex) {
            vertex = graph.get(index).get(1);
        }
        else {
            vertex = graph.get(index).get(0);
        }
    }
    /**
     * Update the best solution
     */
    private void updateBest(Ant ant) {
        if (bestTourOrder == 0) {
            bestTourOrder = ant.Solution.size();
            bestTourLength = solutionscore;
        }
        if (solutionscore < bestTourLength) {
            bestTourLength = solutionscore;
            bestTourOrder = ant.Solution.size();
        }

    }

    /**
     * Clear trails after simulation
     */
    private void clearTrails() {
        trails = new Vector<>();
        IntStream.range(0, graph.size()).mapToDouble(i -> 1.0).forEach(c -> trails.add(c));
    }
    /**
     * Count how many times ant passed through
     */
    private void counter_edges(int vertex){
        counter_edges.set(vertex,+1);
    }
    /**
     * Calculate Soultion score
     */
    private void count(int vertex){
        solutionscore+=weight_of_edge(vertex);
    }
    /**
     * Calculate weight of edge
     */
    private double weight_of_edge(int vertex){
        return weights.get(vertex) * counter_edges.get(vertex);
    }

}