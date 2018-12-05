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
        
        int [] tmpRoute = null;
        int tmpCost = 0;
        long startTime = System.nanoTime();
        
        // 1. initialize
        // 50개의 초기 답 구하기 + 서로 다른지 확인하기
        // FIXME: 굳이 확인해야 하나? 겹칠 확률이 너무 적은데;;;;
        final int POPULATION_SIZE = 50;
        ArrayList<Path> population = new ArrayList<>(POPULATION_SIZE);
        for(int i = 0; i < POPULATION_SIZE; i++){
            tmpRoute = knuthShuffle(mapSize, tmpRoute);
            population.append(new Path(tmpRoute, mapData));
        }

        // 2. mutation
        // 두 답을 섞고 feasible하지 않은 부분을 고치기

        // 3. natural selection
        // population을 초과할 경우, 가장 좋은 답 50개만 남기기

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

    private static int[] knuthShuffle(int mapSize, int[] initialRoute){
        // 초기화
        int [] newRoute = new int[mapSize];

        if (initialRoute != null){
            for(int i = 0; i < mapSize; i++){
                newRoute[i] = initialRoute[i];
            }
        } else {
            for(int i = 0; i < mapSize; i++){
                newRoute[i] = i;
            }
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

class Path implements Comparable<Path>{
    private int[] route;
    private int[][] mapData;
    private int cost;

    private int calculateCost(int[] route, int[][] mapData){
        int cost = 0;
        for(int i = 0; i < route.length; i++){
            cost += mapData[route[i]][route[(i+1)%route.length]];
        }
        return cost;
    }

    public Path(int[] route, int[][] mapData){
        // 경로 검증은 고민하지 않음
        this.route = route;
        this.mapData = mapData;
        this.cost = calculateCost(this.route, this.mapData);
    }

    public int[] getRoute(){
        return this.route;
    }

    public int getCost(){
        return this.cost;
    }

    @Override
    public int compareTo(Path otherPath){
        return Integer.compare(this.cost, otherPath.getCost());
    }

    //TODO: equals 구현
}