/*
Example call cmd line
    Compiling cmd line : javac AI.java
    Execution cmd line : java AI tsp1000.csv result.txt

    @2018-11-06, in AI class, cau MI lab, 2018.
*/
import com.sun.org.apache.bcel.internal.generic.POP;

import java.io.*;
import java.util.*;

public class AI{
    public static void main(String[] args){
        // Assume AI.class [MapDataFilePath] [OutputFilePath]
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);
        final int mapSize = 1000;
        // File Reading
        int [][] mapData = fileLoader(mapSize, inputFile);

        int [] tmpRoute = null;
        long startTime = System.nanoTime();
        final int maxTrial = 1000;
        PathCostComparator comparator = new PathCostComparator();

        // 1. initialize
        // 50개의 초기 답 구하기 + 서로 다른지 확인하기
        final int POPULATION_SIZE = 50;
        TreeSet<Path> population = new TreeSet<>(comparator);
        for(int i = 0; i < POPULATION_SIZE; i++){
            tmpRoute = knuthShuffle(mapSize, tmpRoute);
            population.add(new Path(tmpRoute, mapData));
//            population.add(new Path(greedysearch(i, mapSize, mapData), mapData));
        }
        /*for(int i = 50; i < mapSize; i++){
            Path tmpPath = new Path(greedysearch(i, mapSize, mapData), mapData);
            if(tmpPath.getCost() < population.last().getCost()) {
                population.pollLast();
                population.add(tmpPath);
            }
        }*/

        long timeLimit = startTime + 30000000000L;
        while (population.first().getCost() > 4200 || System.nanoTime() < timeLimit){
            // 2. offspring
            // 답을 만들 부모 선정
            while(population.size() > POPULATION_SIZE - 20){
                population.pollLast();
            }
            ParentSelector parentSelector = new ParentSelector(population.toArray(new Path[0]));

            while (population.size() < POPULATION_SIZE){
                // 답 만들기, 절반 crossover
                Path parent1 = parentSelector.getRandomParent();
                Path parent2 = parentSelector.getRandomParent();
                Path child1 = crossover(parent1, parent2);
                Path child2 = crossover(parent2, parent1);

                // 3. natural selection
                population.add(child1);
                population.add(child2);
            }
        }

        // 4. return result
        int[] resultRoute = population.first().getRoute();

        // reordering
        int startIdx = 0;
        for(int i = 0; i < mapSize; i++){
            if(resultRoute[i] == 0){
                startIdx = i;
                break;
            }
        }
        tmpRoute = new int[mapSize];
        for(int i = 0; i < mapSize; i++){
            tmpRoute[i] = resultRoute[(startIdx+i)%mapSize];
        }

        resultRoute = tmpRoute;
        System.out.println("time: " + ((System.nanoTime() - startTime)/1000000000));
        // File Writing
        resultWriter(resultRoute, mapData, outputFile);
    }

    private static Path crossover(Path parent1, Path parent2){
        int[] parent1Route = parent1.getRoute();
        int[] parent2Route = parent2.getRoute();
        int halfPathLength = parent1Route.length / 2;
        int[] child = new int[parent1Route.length];
        Random random = new Random();
        BitSet visited = new BitSet(parent1Route.length);
        for(int i = 0; i < halfPathLength; i++){
            visited.set(parent1Route[i]);
            child[i] = parent1Route[i];
        }
        for(int i = halfPathLength; i < parent1Route.length; i++){
            if(visited.get(parent2Route[i])){
                int fromIndex = random.nextInt(parent1Route.length);
                int alternativeNode = visited.previousClearBit(fromIndex);
                if(alternativeNode == -1){
                    alternativeNode = visited.nextClearBit(fromIndex);
                }
                visited.set(alternativeNode);
                child[i] = alternativeNode;
            } else {
                visited.set(parent2Route[i]);
                child[i] = parent2Route[i];
            }
        }

        // mutation - exchange
        if(random.nextDouble() < 0.001){
            int swapA = random.nextInt(child.length);
            int swapB = random.nextInt(child.length);
            int tmp = child[swapA];
            child[swapA] = child[swapB];
            child[swapB] = tmp;
        }
        return new Path(child, parent1.getMapData());
    }

    private static int[] knuthShuffle(int mapSize, int[] initialRoute){
        // 초기화
        int [] newRoute = new int[mapSize];

        if (initialRoute != null){
            System.arraycopy(initialRoute, 0, newRoute, 0, mapSize);
        } else {
            for(int i = 0; i < mapSize; i++){
                newRoute[i] = i;
            }
        }

        // 셔플
        Random random = new Random();
        int tmp, swapIdx;
        for(int i = mapSize - 1; i > 0; i--){
            swapIdx = random.nextInt(i+1);
            tmp = newRoute[i];
            newRoute[i] = newRoute[swapIdx];
            newRoute[swapIdx] = tmp;
        }

        return newRoute;
    }

    private static int[] greedysearch(int start, int mapSize, int[][] mapData){
        BitSet visited = new BitSet(mapSize);
        int[] route = new int[mapSize];
        route[0] = start;
        visited.set(start);
        for(int i = 1; i < route.length; i++){
            int currentCityNo = route[i-1];
            int minAdjacentEdgeCost = 1000;
            int minAdjacentEdgeDst = 0;
            // find least adjacent edge
            for(int j = 0; j < mapSize; j++){
                if(j == currentCityNo) continue;
                if(mapData[currentCityNo][j] < minAdjacentEdgeCost && !visited.get(j)){
                    minAdjacentEdgeCost = mapData[currentCityNo][j];
                    minAdjacentEdgeDst = j;
                }
            }

            route[i] = minAdjacentEdgeDst;
            visited.set(minAdjacentEdgeDst);
        }
        return route;
    }

    public static int [][] fileLoader(int nodeSize, File iFile){
        int [][] RET = new int[nodeSize][nodeSize];
        String line = "";
        try{
            BufferedReader bufferReader = new BufferedReader(new FileReader(iFile));
            int row=0;
            while((line=bufferReader.readLine())!=null){
                String[] strNum = line.split(",",-1);
                for(int col=0;col<nodeSize;col++){
                    RET[row][col] = Integer.parseInt(strNum[col]);
                }
                row++;
                if(row>=nodeSize){
                    break;
                }
            }
            bufferReader.close();
        } catch(IOException e){
            e.printStackTrace();
        }
        return RET;
    }

    public static int resultWriter(int[] route, int[][] map, File oFile){
        int cost = 0;
        int [] checkroute = new int[route.length];
        for(int i=0;i<route.length;i++){
            checkroute[route[i]] += 1;
        }
        int check=1;
        for(int i=0;i<route.length;i++){
            check *= checkroute[i];
        }
        if(check != 1){
            System.out.println("Duplicated city in route!");
            return -1; //Error! Error!
        }
        else{
            System.out.println("Check route: OK");
            // Calculate a cost of route.
            for(int i=0;i<route.length;i++){
                cost += map[route[i]][route[(i+1)%route.length]];
            }
            System.out.println("Cost: "+cost);
        }

        try{
            FileWriter fWriter = new FileWriter(oFile);
            fWriter.write(cost+"\r\n");
            for (int i=0;i<route.length;i++){
                fWriter.write(route[i]+"\r\n");
            }
            fWriter.close();
        } catch(IOException e){
            System.out.print(cost+"\r\n");
            for (int i=0;i<route.length;i++){
                System.out.print(route[i]+"\r\n");
            }
            e.printStackTrace();
        }
        return 0;
    }
}

class Path{
    private int[] route;
    private int[][] mapData;
    private int cost;

    private int calculateCost(int[] route, int[][] mapData){
        int cost = 0;
        int loopLimit = route.length-1;
        for(int i = 0; i < loopLimit; i++){
            cost += mapData[route[i]][route[i+1]];
        }
        cost += mapData[route[loopLimit]][route[0]];
        return cost;
    }

    Path(int[] route, int[][] mapData){
        this.route = route;
        this.mapData = mapData;
        this.cost = calculateCost(this.route, this.mapData);
    }

    int[] getRoute(){
        return this.route;
    }

    int getCost(){
        return this.cost;
    }

    int[][] getMapData() {
        return mapData;
    }
}

class ParentSelector {
    private Path[] parents;
    private int minCost;
    private int maxCost;
    private double[] fitnessValues;
    private double totalFitnessValues;

    ParentSelector(Path[] parents){
        this.parents = parents;
        initializeCosts(parents);
        initializeFitnessValue(parents, minCost, maxCost);
    }

    private void initializeCosts(Path[] parents){
        this.maxCost = this.minCost = parents[0].getCost();
        for(Path path : parents){
            this.minCost = Math.min(path.getCost(), this.minCost);
            this.maxCost = Math.max(path.getCost(), this.maxCost);
        }
    }

    private void initializeFitnessValue(Path[] parents, int minCost, int maxCost){
        totalFitnessValues = 0.0;
        fitnessValues = new double[parents.length];
        for(int i = 0; i < parents.length; i++){
            fitnessValues[i] = ((maxCost - parents[i].getCost()) + (maxCost - minCost))/3.0;
            totalFitnessValues += fitnessValues[i];
        }
    }

    Path getRandomParent(){
        double selectedPoint = Math.random() * totalFitnessValues;
        double bound = 0.0;
        for(int i = 0; i < fitnessValues.length; i++){
            bound += fitnessValues[i];
            if(selectedPoint < bound){
                return parents[i];
            }
        }
        return parents[parents.length-1];
    }
}

class PathCostComparator implements Comparator<Path>{
    @Override
    public int compare(Path o1, Path o2) {
        return Integer.compare(o1.getCost(), o2.getCost());
    }
}