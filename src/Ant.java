import java.util.Vector;

class Ant {



    private final Vector<Integer> solution = new Vector<>();
    /**
     * Constructor of each ant
     */
    void visitCity(int index) {
        solution.add(index);
    }

    public Vector<Integer> getSolution() {
        return solution;
    }
    /**
     * adding up the weights of the edges the ant passed through
     */
    double getTrailLength(Vector<Integer> weights) {
        double length = 0;
        for (Integer integer : solution) {
            length += weights.get(integer);
        }
        return length;
    }

}