/*
Example call cmd line
    Compiling cmd line : javac AI.java
    Execution cmd line : java AI tsp1000.csv result.txt

    @2018-11-06, in AI class, cau MI lab, 2018.
*/
import java.io.*;
import java.util.Deque;
import java.util.ArrayDeque;

public class AI{
    public static void main(String[] args){
        // Assume AI.class [MapDataFilePath] [OutputFilePath]
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);
        int mapSize = 1000; // Integer.parseInt(args[3]);

        // File Reading
        int [][] mapData = fileLoader(mapSize, inputFile);
        
        int [] resultRoute = new int[mapSize];
        int resultCost = 0;
        
        // 1. greedy search
        
        resultRoute[0] = 0;
    
        for(int i = 1; i < mapSize; i++){
            int currentCityNo = resultRoute[i-1];
            int minAdjacentEdgeCost = 1000;
            int minAdjacentEdgeDst = 0;
            // find least adjacent edge
            for(int j = 0; j < mapSize; j++){
                if(j == currentCityNo) continue;
                if(mapData[currentCityNo][j] < minAdjacentEdgeCost && !contains(resultRoute, j, i)){
                    minAdjacentEdgeCost = mapData[currentCityNo][j];
                    minAdjacentEdgeDst = j;
                }
            }
            
            resultRoute[i] = minAdjacentEdgeDst;
        }
        for(int i=0;i<mapSize;i++){
            resultCost += mapData[resultRoute[i]][resultRoute[(i+1)%mapSize]];
        }
        
        // 2. hill-climbing search
        int tmpCost = hillClimbing(resultRoute, resultCost, mapData);
        while(tmpCost < resultCost){
            resultCost = tmpCost;
            tmpCost = hillClimbing(resultRoute, resultCost, mapData);
        }
        
        // File Writing
        resultWriter(resultCost, resultRoute, outputFile);
        return;
    }

    private static boolean contains(int[] array, int match, int end){
        for(int i = 0; i < end; i++){
            if(array[i] == match) return true;
        }
        return false;
    }
    
    private static int hillClimbing(int[] resultRoute, int resultCost, int[][] mapData){
        for(int i = 1; i < resultRoute.length; i++){
            for(int k = i+1; k < resultRoute.length; k++){
                int[] newRoute = twoOptSwap(resultRoute, i, k);
                int newResultCost = 0;
                for(int j=0;j<resultRoute.length;j++){
                    newResultCost += mapData[newRoute[j]][newRoute[(j+1)%resultRoute.length]];
                }
                if (newResultCost < resultCost){
                    resultRoute = newRoute;
                    return newResultCost;
                }
            }
        }
        return resultCost;
    }
    
    private static int[] twoOptSwap(int[] existing_route, int swap_start, int swap_end){
        int [] new_route = new int[existing_route.length];
        for(int i = 0; i < existing_route.length; i++){
            if(i >= swap_start && i <= swap_end){
                new_route[i] = existing_route[swap_end - (i - swap_start)];
            }
            else{
                new_route[i] = existing_route[i];
            }
        }
        return new_route;
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

    public static int resultWriter(int cost, int[] route, File oFile){
        try{
            FileWriter fWriter = new FileWriter(oFile);
            fWriter.write(cost+"\n");
            for (int i=0;i<route.length;i++){
                fWriter.write(route[i]+" ");
            }
            fWriter.close();
        } catch(IOException e){
            System.out.println(""+cost);
            for (int i=0;i<route.length;i++){
                System.out.print(route[i]+" ");
            }
            e.printStackTrace();
        }
        return 0;
    }
}