import java.util.Vector;

class Ant {


    Vector<Integer> Solution;
    /**
     * Constructor of each ant
     */
    void visitCity(int index) {
        Solution.add(index);
    }

    /**
     * adding up the weights of the edges the ant passed through
     */
    double trailLength(Vector<Integer> weights) {
        double length = 0;
        for (Integer integer : Solution) {
            length += weights.get(integer);
        }
        return length;
    }

}