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

        // File Reading
        int [][] mapData = fileLoader(mapSize, inputFile);

        // Blah Blah Blah
        int [] resultRoute = new int[mapSize]; // start city number is 0.
        int resultCost = 0;
        /*
        for(int i=0;i<mapSize;i++){
            resultRoute[i] = i;
        }
        */
        for(int i=0;i<mapSize;i++){
            resultCost += mapData[resultRoute[i]][resultRoute[(i+1)%mapSize]];
        }
        // Blah Blah Blah

        // File Writing
        resultWriter(resultCost, resultRoute, outputFile);
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

    public static int resultWriter(int cost, int[] route, File oFile){
        int [] checkroute = new int[route.length];
        for(int i=0;i<route.length;i++){
            checkrout[route[i]] += 1;
        }
        int check=1;
        for(int i=0;i<route.length;i++){
            check *= checkout[i];
        }
        if(check != 1)
            return -1; //Error! Error!
        try{
            FileWriter fWriter = new FileWriter(oFile);
            fWriter.write(cost+"\n");
            for (int i=0;i<route.length;i++){
                fWriter.write(route[i]+"\n");
            }
            fWriter.close();
        } catch(IOException e){
            System.out.print(cost+"\n");
            for (int i=0;i<route.length;i++){
                System.out.print(route[i]+"\n");
            }
            e.printStackTrace();
        }
        return 0;
    }
}
