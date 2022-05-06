import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.IntStream;

class AntColonyOptimization {

    private int numberOfCities;
    private float solutionScore;
    private final double evaporation;
    private final int q;
    private int vertex;
    private int bestTourOrder;
    private float bestTourLength;
    private Vector<Vector<Integer>> graph;
    private Vector<Integer> weights;
    private Vector<Double> trails;
    private Vector<Integer> counterEdges;
    private final List<Ant> ants = new ArrayList<>();
    private final Random random = new Random();
    private Vector<Double> probabilities;

    /**
     * Constructor
     */
    AntColonyOptimization(double eParam, int qParam, String fileName) throws FileNotFoundException {
        load(fileName);
        for (int i = 0; i < 5; i++) ants.add(new Ant());
        evaporation = eParam;
        q = qParam;
    }

    /**
     * Main method of ACO
     */
    void solve() {

        clearTrails();

        int maxIterations = 5;
        for (int i = 0; i < maxIterations; i++) {
            moveAnts();
            updateTrails();
        }

        System.out.println("Best tour length: " + bestTourLength);
        System.out.println("Best tour order: " + bestTourOrder);
    }

    /**
     * At each iteration, move ants
     */
    private void moveAnts() {
            ants.forEach(ant -> {
                solutionScore =0;
                vertex = random.nextInt(numberOfCities);
                Vector<Integer> usedVertexes = new Vector<>();
                while(usedVertexes.size()<numberOfCities) {
                    int index = selectNextCity(ant);
                    ant.visitCity(index);
                    countEdges(index);
                    count(index);
                    updateActual(index);
                    if(!usedVertexes.contains(vertex))
                        usedVertexes.add(vertex);
                }
                updateBest(ant);
            });
    }

    /**
     * Add Neighborhood
     */
    private Vector<Integer> getNeighborhood(){
        Vector<Integer> neighborhood = new Vector<>();
        IntStream.range(0, graph.size()).filter(i -> (
                (graph.get(i).get(0) == vertex || graph.get(i).get(1) == vertex))).forEach(neighborhood::add);
        return neighborhood;
    }
    /**
     * Check if all neighbor is used in neighborhood
     */
    private boolean checkAllUsed(Ant ant, Vector<Integer> neighborhood){
        boolean allUsed = true;
        for (Integer iterNeighborhood : neighborhood) {
            if (!ant.getSolution().contains(iterNeighborhood)) {
                allUsed = false;
                break;
            }
        }
        return allUsed;
    }
    /**
     * Add all unused neighbor
     */
    private Vector<Integer> checkAllUnused(Ant ant, Vector<Integer> neighborhood){
        Vector<Integer>notAllUsed = new Vector<>();
        for (Integer neighborhoods : neighborhood) {
            if (!ant.getSolution().contains(neighborhoods)) {
                notAllUsed.add(neighborhoods);
            }
        }
        return notAllUsed;
    }
    /**
     * Find last time used edges in solution
     */
    private Vector<Integer> findLastTimeUsedEdge(Vector<Integer> neighborhood, Ant ant){
        Vector<Integer> lastTimeUsedEdge = new Vector<>();
        for (int j = 0; j < neighborhood.size(); j++) {
            lastTimeUsedEdge.add(0);
        }

        for (int i = 0; i<ant.getSolution().size(); i++) {
            for(int j = 0; j< neighborhood.size(); j++) {
                if (neighborhood.get(j).equals(ant.getSolution().get(i))) {
                    lastTimeUsedEdge.set(j, i);
                }
            }
        }
        return lastTimeUsedEdge;
    }
    /**
     * Select next city for each ant
     */
    private int selectNextCity(Ant ant) {
        Vector<Integer> neighborhood = getNeighborhood();
        boolean allUsed = checkAllUsed(ant, neighborhood);
        if(allUsed) {

            Vector<Integer> lastTimeUsedEdge = findLastTimeUsedEdge(neighborhood, ant);

            int maxLastItem = Collections.min(lastTimeUsedEdge);
            int index = lastTimeUsedEdge.indexOf(maxLastItem);
            return neighborhood.get(index);
        }else {
            Vector<Integer>notAllUsed = checkAllUnused(ant, neighborhood);
            calculateProbabilities(notAllUsed);
            double r = random.nextDouble(), total = 0;
            for (int i=0; i<notAllUsed.size(); i++) {
                    total += probabilities.get(i);
                    if (total >= r) {
                        return notAllUsed.get(i);
                    }
            }
        }
        throw new RuntimeException("No other cities");
    }

    /**
     * Calculate the next city picks probabilities
     */
    private void calculateProbabilities(Vector<Integer> notAllUsed) {
        double pheromone = 0.0;
        float alpha = 1;
        double beta = 5.0;
        probabilities = new Vector<>();
        for (Integer next : notAllUsed) {
                pheromone += Math.pow(trails.get(next), alpha) * Math.pow(1.0 /weights.get(next), beta);
        }
        for (Integer next : notAllUsed) {
                double numerator = Math.pow(trails.get(next), alpha) * Math.pow(1.0 / weights.get(next), beta);
                probabilities.add(numerator / pheromone);
        }
    }

    /**
     * Update trails that ants used
     */
    private void updateTrails() {
        IntStream.range(0, graph.size()).forEach(i -> trails.set(i, trails.get(i) * evaporation));
        for (Ant a : ants) {
            double contribution = q / a.getTrailLength(weights);
            for (int i = 0; i < a.getSolution().size(); i++) {
                trails.set(a.getSolution().get(i), +contribution) ;
            }
        }
    }
    /**
     * Read graph and weights from file
     */
    private void load(String fileName) throws FileNotFoundException {
        int number;
        Scanner in = new Scanner(new File(fileName));

        graph = new Vector<>();
        weights = new Vector<>();
        while (in.hasNext()) {
            Vector<Integer> edge = new Vector<>();
            number = in.nextInt();
            if(number > numberOfCities) numberOfCities = number;
            edge.add(number);
            number = in.nextInt();
            if(number > numberOfCities) numberOfCities = number;
            edge.add(number);
            graph.add(edge);
            weights.add(in.nextInt());
        }
        counterEdges = new Vector<>();
        for (int i = 0; i < graph.size(); i++) {
            counterEdges.add(0);
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
            bestTourOrder = ant.getSolution().size();
            bestTourLength = solutionScore;
        }
        if (solutionScore < bestTourLength) {
            bestTourLength = solutionScore;
            bestTourOrder = ant.getSolution().size();
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
    private void countEdges(int vertex){
        counterEdges.set(vertex,+1);
    }
    /**
     * Calculate Solution score
     */
    private void count(int vertex){
        solutionScore += getWeightedEdges(vertex);
    }
    /**
     * Calculate weight of edge
     */
    private double getWeightedEdges(int vertex){
        return weights.get(vertex) * counterEdges.get(vertex);
    }

}