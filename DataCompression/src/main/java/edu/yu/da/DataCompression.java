package edu.yu.da;

import javax.naming.SizeLimitExceededException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class DataCompression implements DataCompressionI{
    /** Constructor.
     *
     * @param original the list whose elements we want to reorder
     * to reduce the
     * number of bytes when compressing the list.
     */
    List<String> copy = new ArrayList<String>();
    List<String> original;
    public DataCompression(final List<String> original) {
        this.original = original;
        copy.addAll(original);
        nCompressedBytesInOriginalList = DataCompressionI.bytesCompressed(original);
    }
    int nCompressedBytesInOriginalList;
    public SolutionI solveIt(GeneticAlgorithmConfig gac) {
        Solution best = new Solution(new ArrayList<String>(),0,0);
        int popSize = gac.getInitialPopulationSize();
        Solution[] sols = new Solution[popSize];

        for(int i = 0; i < popSize; i++){
            sols[i] = new Solution(new ArrayList<String>(),0,0);
            sols[i].setList(randomizeList(copy));
        }

        for(int gen = 0; gen < gac.getMaxGenerations(); gen++) {
            if(gac.getSelectionType() == GeneticAlgorithmConfig.SelectionType.ROULETTE){
                Solution[] newSolutions = new Solution[gac.getInitialPopulationSize()];
                int total = 0;
                for (Solution s : sols) {
                    double d = s.fitness;
                    total += (fitnessTest(s.fitness));
                }
                Solution[] rol = new Solution[total];
                int upTo = 0;
                int counter=0;
                for(Solution s : sols){
                    for(int i = 0; i < fitnessTest(s.fitness); i++){
                        if(upTo >= total){
                            break;
                        }
                        rol[upTo++] = new Solution(s.getList(), s.fitness, s.generations);
                    }
                }
                Random r = new Random();
                for(int i = 0; i < popSize; i++){
                    newSolutions[i] = rol[r.nextInt(total)];
                }
                sols = newSolutions;
            }else{//Tourtament
                Solution[] newSols = new Solution[popSize];
                if(popSize > 5){
                    for(int i = 0; i < ((popSize / 5) * 5) ; i++){
                        Solution greatest = new Solution(new ArrayList<String>(),0,0);
                        double bestVal = Integer.MIN_VALUE;
                        int setter = i;
                        for(int j = 0; j < 5; j++){
                            if(sols[setter].fitness > bestVal){
                                bestVal = sols[setter].fitness;
                                greatest = sols[setter];
                            }
                            setter++;
                        }
                        for(int j = 0; j < 5; j++) {
                            newSols[i++] = new Solution(greatest.getList(), greatest.getFitness(), greatest.nGenerations());
                        }
                        i--;
                    }
                    int j = 0;
                    for(int i = ((popSize / 5) * 5); i < popSize; i++){
                        newSols[i] = new Solution(newSols[j].getList(), newSols[j].getFitness(), newSols[j].nGenerations());
                        j+=5;
                    }
                }
                sols = newSols;
            }

            for(int i = 0; i < sols.length - 1; i++){
                sols[i] = mutate(sols[i], gac.getMutationProbability());
                double rand = Math.random();
                if(gac.getCrossoverProbability() < rand){
                    if(i < (sols.length - 1)) {
                        sols[i] = crossover(sols[i], sols[i + 1]);
                        sols[i+1] = crossover(sols[i + 1], sols[i]);
                        sols[i].nextGen();
                        i++;
                    }
                }
                sols[i].nextGen();
            }
            for(Solution i : sols) {
                if (i.getFitness() >= gac.getThreshold()) {
                    return i;
                }
            }
        }
        double bestFit = Integer.MIN_VALUE;
        for(Solution i : sols) {
            if(i.fitness > bestFit){
                bestFit = i.fitness;
                best = i;
            }
        }
        if(best.fitness < 1.001){
            Collections.shuffle(original);
            return new Solution(original, 0, best.generations);
        }
        return best;
    }

    @Override
    public int nCompressedBytesInOriginalList() {
        return nCompressedBytesInOriginalList;
    }
    private List<String> randomizeList(List<String> list){
        Collections.shuffle(list);
        return list;
    }

    public class Solution implements SolutionI {
        private int x;
        private int y;
        List<String> list;
        private double fitness;
        private int generations;
        double currentCompressed;
        public Solution(List<String> list, double fitness, int nGenerations){
            setList(list);
            setFitness();
            this.fitness = fitness;
        }

        /**
         * Returns the fitness of this solution.
         */
        public double getFitness() {
            setFitness();
            return fitness;
        }
        private void setFitness(){
            fitness = relativeImprovement();
        }
        private void setList(List<String> list){
            this.list = list;
            setFitness();
        }

        @Override
        public List<String> getList() {
            return list;
        }

        @Override
        public List<String> getOriginalList() {
            return original;
        }

        @Override
        public double relativeImprovement() {
            currentCompressed = DataCompressionI.bytesCompressed(list);
            return nCompressedBytesInOriginalList/currentCompressed;
        }

        /**
         * Returns the number of generations that the genetic algorithm ran to
         * produce this solution.
         */
        public int nGenerations() {
            return generations;
        }
        public void nextGen(){
            generations++;
        }

    }
    private Solution mutate(Solution s, double prob){
        //double rand = Math.random();
        Random random = new Random();
        if (random.nextDouble() < prob) {//then mutate. Now choose what to mutate.
            List<String> list = s.getList();
            int rand1 = random.nextInt(list.size());
            int rand2 = random.nextInt(list.size());
            String external = list.get(rand1);
            list.set(rand1, list.get(rand2));
            list.set(rand2, external);
            s.setList(list);
        }

        return s;
    }
    private Solution crossover(Solution one, Solution two){
        Random r = new Random();
        String[] list = new String[one.getList().size()];
        int start = r.nextInt(one.getList().size()/2);
        HashMap<String, Integer> amountOfEach = new HashMap<>();
        for(String s : one.getList()){
            int amount = amountOfEach.getOrDefault(s, 0);
            amountOfEach.put(s,++amount);
        }
        List<String> oneList = one.getList();
        for(int i = start; i < start + one.getList().size()/2; i++){
            String firstS = oneList.get(i);
            int amount = amountOfEach.get(firstS);
            if(amount == 1){
                amountOfEach.remove(firstS);
            }else {
                amountOfEach.put(firstS, --amount);
            }
            list[i] = firstS;
        }
        int counter = 0;
        for(int i = 0; i < list.length; i++){
            if(list[i] == null){
                while (true) {
                    List<String> secondS = two.getList();
                    String current = secondS.get(counter);
                    if(amountOfEach.containsKey(current)){
                        list[i] = current;
                        if(amountOfEach.get(current) == 1){
                            amountOfEach.remove(current);
                        }else {
                            int amount = amountOfEach.get(current);
                            amountOfEach.put(current, --amount);
                        }
                        break;
//                        if(amountOfEach.size() == 0){
//                            break;
//                        }
                    }
                    counter++;
                }
            }
        }
        one.setList(Arrays.asList(list));
        return one;
    }
    private int fitnessTest(double f){
        if(f > 1.1){
            return 50;
        }else if(f > 1.07){
            return 20;
        }else if(f > 1.05){
            return 10;
        } else if(f > 1.03){
            return 5;
        }else if(f > 1.02){
            return 4;
        } else if(f > 1.01){
            return 2;
        } else if(f >= 1){
            return 1;
        }else{
            return 0;
        }
    }
}

