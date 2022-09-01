package edu.yu.da;

import java.util.Random;

public class SimpleEquation implements SimpleEquationI {
    public SimpleEquation() {

    }
    private double fitMap(double fit){
        if(fit > 5){
            return 400;
        }else if(fit > 0){
            return 100;
        }else if(fit > -250){
            return 50;
        }else if(fit > -750){
            return 25;
        }else if(fit > -1500){
            return 13;
        }else if(fit > -3000){
            return 4;
        }else if(fit > -5000){
            return 3;
        }else if(fit > -10000){
            return 2;
        }else{
            return 1;
        }
    }
    @Override
    public SolutionI solveIt(GeneticAlgorithmConfig gac) {

        Solution best = new Solution(0,0,0,0);
        int popSize = gac.getInitialPopulationSize();
        double[] randomNums = new double[popSize];
        Random randomGen = new Random();
        for(int i = 0; i < popSize; i++){
            randomNums[i] = randomGen.nextDouble();
        }
        Solution[] sols = new Solution[popSize];
        for(int i = 0; i < popSize; i++) {
            int x = (int) (Math.random() * 100);
            int y = (int) (Math.random() * 100);
            sols[i] = new Solution(x, y, 0, 0);
        }
        int r = 0;
        for(int gen = 0; gen < gac.getMaxGenerations(); gen++){
            if(gac.getSelectionType() == GeneticAlgorithmConfig.SelectionType.ROULETTE){
                Solution[] newSols = new Solution[gac.getInitialPopulationSize()];
                int total = 0;
                for (Solution s : sols) {
                    //System.out.print(s.fitness + " ");
                    total += fitMap(s.fitness);
                }
                Solution[] rol = new Solution[total];
                int upTo = 0;
                for(Solution s : sols){
                    for(int i = 0; i < fitMap(s.fitness); i++){
                        rol[upTo++] = new Solution(s.x,s.y, s.fitness, s.generations);
                    }
                }
                for(int i = 0; i < popSize; i++){
                    newSols[i] = rol[randomGen.nextInt(total)];
                }
                sols = newSols;
            }
            else if(gac.getSelectionType() == GeneticAlgorithmConfig.SelectionType.TOURNAMENT){
                Solution[] newSols = new Solution[popSize];
                if(popSize > 5){

                    for(int i = 0; i < ((popSize / 3) * 3) ; i++){
                        Solution greatest = new Solution(0,0,0,0);
                        double bestVal = Integer.MIN_VALUE;
                        int setter = i;
                        for(int j = 0; j < 4; j++){
                            if(sols[setter].fitness > bestVal){
                                bestVal = sols[setter].fitness;
                                greatest = sols[setter];
                            }
                            setter++;
                        }
                        for(int j = 0; j < 3; j++) {
                            newSols[i++] = new Solution(greatest.getX(), greatest.getY(), greatest.fitness(), greatest.nGenerations());
                        }
                        i--;
                    }
                    int j = 0;
                    for(int i = ((popSize / 3) * 3); i < popSize; i++){
                        newSols[i] = new Solution(newSols[j].getX(), newSols[j].getY(), newSols[j].fitness(), newSols[j].nGenerations());
                        j+=3;
                    }
                }
                int h = 0;
                h++;
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
                if (i.fitness >= gac.getThreshold()) {
                    return i;
                }
            }
        }
        int bestFit = Integer.MIN_VALUE;
        for(Solution i : sols) {
            if(i.fitness > bestFit){
                bestFit = (int) i.fitness;
                best = i;
            }
        }
        return best;
    }
    private Solution mutate(Solution s, double prob){
        //double rand = Math.random();
        Random random = new Random();

        if(random.nextDouble() < prob){
            if(random.nextDouble() > 0.5){
                if(random.nextDouble() > 0.5){
                    s.setX(s.x - random.nextInt(3));
                }else{
                    s.setX(s.x + random.nextInt(3));
                }
            }else{
                if(random.nextDouble() > 0.5){
                    s.setY(s.y - random.nextInt(3));
                }else{
                    s.setY(s.y + random.nextInt(3));
                }
            }
        }
        return s;
    }
    private Solution crossover(Solution one, Solution two){
        return new Solution(one.x, two.y, 0, one.generations);
    }


    public class Solution implements SimpleEquationI.SolutionI {
        private int x;
        private int y;
        private double fitness;
        private int generations;
        public Solution(int x, int y, double fitness, int nGenerations){
            this.x = x;
            this.y = y;
            this.fitness = fitness;
            if(fitness == 0){
                setFitness();
            }
            this.generations = nGenerations;
        }

        /**
         * Returns the value of x for the equation's solution.
         */
        public int getX() {
            return x;
        }
        private void setX(int x){
            if(x < 0){
                x = 0;
            }else if(x > 99){
                x = 99;
            }
            this.x = x;
            setFitness();
        }
        /**
         * Returns the value of y for the equation's solution.
         */
        public int getY() {
            return y;
        }
        private void setY(int y){
            if(y < 0){
                y = 0;
            }else if(y >99){
                y = 99;
            }
            this.y = y;
            setFitness();
        }

        /**
         * Returns the fitness of this solution.
         */
        public double fitness() {
            return fitness;
        }
        private void setFitness(){
            int value = 6*x - x*x + 4*y - y*y;
            this.fitness = value;
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
}
