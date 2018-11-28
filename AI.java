/*
Example call cmd line
    Compiling cmd line : javac AI.java
    Execution cmd line : java AI tsp1000.csv result.txt

    @2018-11-06, in AI class, cau MI lab, 2018.
*/
import java.io.*;
import java.util.Random;
import java.util.BitSet;

public class AI{
    public static void main(String[] args){
        // Assume AI.class [MapDataFilePath] [OutputFilePath]
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);
        int mapSize = 1000; // Integer.parseInt(args[3]);
        // File Reading
        int [][] mapData = fileLoader(mapSize, inputFile);
        
        int [] resultRoute;
        int [] tmpRoute;
        int resultCost = 0;
        int tmpCost = 0;
        long startTime = System.nanoTime();
        // resultRoute = knuthShuffle(mapSize);
        // resultCost = getCost(resultRoute, mapData);
        // 1. greedy search
        resultRoute = greedysearch(0, mapSize, mapData);
        resultCost = getCost(resultRoute, mapData);
        for(int i = 1; i < mapSize; i++){
            tmpRoute = greedysearch(i, mapSize, mapData);
            tmpCost = getCost(tmpRoute, mapData);
            if(tmpCost < resultCost){
                resultCost = tmpCost;
                resultRoute = tmpRoute;
            }
        }
        
        /* // 2. hill-climbing search
        long timeLimit = startTime + 30000000000L;
        do{
            tmpRoute = stochasticHillClimbing(resultRoute, resultCost, mapData);
            tmpCost = getCost(tmpRoute, mapData);
            resultRoute = tmpRoute;
            resultCost = tmpCost;
        }while(System.nanoTime() < timeLimit); */

        // 2. simulated annealing
        resultRoute = simulatedAnnealing(resultRoute, resultCost, mapData);

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
        return;
    }

    private static int getCost(int[] route, int[][] mapData){
        int cost = 0;
        for(int i=0;i<route.length;i++){
            cost += mapData[route[i]][route[(i+1)%route.length]];
        }
        return cost;
    }

    private static int[] knuthShuffle(int mapSize){
        // 초기화
        int [] newRoute = new int[mapSize];
        for(int i = 0; i < mapSize; i++){
            newRoute[i] = i;
        }

        // 셔플
        Random random = new Random();
        int tmp, swapIdx = 0;
        for(int i = mapSize - 1; i > 0; i--){
            swapIdx = random.nextInt(i + 1);
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
    
    private static int[] simulatedAnnealing(int[] resultRoute, int resultCost, int[][] mapData){
        Random random = new Random();
        double temperature = 3.899;
        double coolingRatio = 0.9999998;
        int[] bestRoute = resultRoute;
        int bestCost = resultCost;
        
        int[] tmpRoute;
        int tmpCost, swapStart, swapEnd;

        while (temperature > 1) {
            do{
                swapStart = random.nextInt(resultRoute.length);
                swapEnd = random.nextInt(resultRoute.length);
            }while(swapStart >= swapEnd);
            
            tmpRoute = twoOptSwap(resultRoute, swapStart, swapEnd);
            tmpCost = getCost(tmpRoute, mapData);
            if(random.nextDouble() < getAcceptanceProbability(tmpCost, resultCost, temperature)){
                resultRoute = tmpRoute;
                resultCost = tmpCost;
            }
            if(resultCost < bestCost){
                bestRoute = resultRoute;
                bestCost = resultCost;
            }

            temperature *= coolingRatio;
        }

        return bestRoute;
    }

    private static double getAcceptanceProbability(int tmpCost, int resultCost, double temperature){
        if (tmpCost < resultCost){
            return 1.0;
        }
        return Math.exp((resultCost - tmpCost) / temperature);
    }

    private static int[] stochasticHillClimbing(int[] resultRoute, int resultCost, int[][] mapData){
        Random random = new Random();
        int swapStart, swapEnd, tmpCost;
        int[] tmpRoute;
        final int neighbors = 31;
        int[][] newRoutes = new int[neighbors][];
        newRoutes[0] = resultRoute;
        // 모든 시도를 해도 찾지 못할 경우 맨 아래 줄에서 resultRoute를 반환함.
        int count = 1;
        for(int trial = 0; trial < 1500; trial++){
            do{
                swapStart = random.nextInt(resultRoute.length);
                swapEnd = random.nextInt(resultRoute.length);
            }while(swapStart >= swapEnd);
            
            tmpRoute = twoOptSwap(resultRoute, swapStart, swapEnd);
            tmpCost = getCost(tmpRoute, mapData);
            if(tmpCost < resultCost){
                newRoutes[count] = tmpRoute;
                count++;
                if (count == neighbors){
                    return newRoutes[random.nextInt(neighbors)];
                }
            }
        }

        return newRoutes[random.nextInt(count)];
    }
    
    private static int[] twoOptSwap(int[] existingRoute, int swapStart, int swapEnd){
        int [] newRoute = new int[existingRoute.length];
        for(int i = 0; i < existingRoute.length; i++){
            if(i >= swapStart && i <= swapEnd){
                newRoute[i] = existingRoute[swapEnd - (i - swapStart)];
            }
            else{
                newRoute[i] = existingRoute[i];
            }
        }
        return newRoute;
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