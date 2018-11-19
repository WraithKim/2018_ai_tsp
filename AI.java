/*
Example call cmd line
    Compiling cmd line : javac AI.java
    Execution cmd line : java AI tsp1000.csv result.txt

    @2018-11-06, in AI class, cau MI lab, 2018.
*/
import java.io.*;

public class AI{
    public static void main(String[] args){
        // Assume call cmd: java AI [MapDataFilePath] [OutputFilePath]
        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);
        int mapSize = 1000; // Integer.parseInt(args[2]);
        int [] resultRoute = new int[mapSize]; // start city number is 0.

        // File Reading
        int [][] mapData = fileLoader(mapSize, inputFile);

        // There is a example code. Write your algorithm
        for(int i=0;i<mapSize;i++){
            resultRoute[i] = i;
        }
        
        // File Writing
        resultWriter(resultRoute, mapData, outputFile);
        return;
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