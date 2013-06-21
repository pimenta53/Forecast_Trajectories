package terrain;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Scanner;

import ontology.Point2D;
import ontology.Way;


public class Terrain extends Observable {
	
	public static int range = 10; //variavel global que indica o raio das antenas a serem criadas

	public static int sizeW = 80;
	public static int sizeH = 80;
	// pontos por defeito, são alterados com a leitura de terreno
	public static Point2D start = new Point2D(0,0) ;
	public static Point2D end = new Point2D(0,79);
	
	/* letra "t" terreno // verde
	 * letra "p" para parede //preto
	 * letra "s" significa que tem ali um sensor // branco
	 * letra "v" vigiado por um sensor // vermelho
	 * letra "w" significa que tem ali walker // azul
	 * letra "a" tentativa de caminho // amarelo
	 * letra "c" tentativa acertada // azul bebe
	*/
	
	public static String matrix[][] = new String[sizeH][sizeW];
		
		//Constructor
	public Terrain(){

			for(int i = 0; i < sizeH; i++)
				for(int j = 0; j < sizeW; j++)
					matrix[i][j] = null;
	}
		
		
		//Methods

	public String getCell(int i, int j) {return matrix[i][j];}
		
	public static Point2D getStart() {
		return start;
	}

	public static void setStart(Point2D start) {
		Terrain.start = start;
	}

	public static Point2D getEnd() {
		return end;
	}

	public static void setEnd(Point2D end) {
		Terrain.end = end;
	}

	public void setCell(int i, int j, String value) {
		matrix[i][j] = value;
    	String nome = "" + i + "," + j;
    	setChanged();
    	notifyObservers(nome);
	}
		
	// verifica se um ponto se encontra na area de um circulo
	public static boolean inArea(int x,int y,int a,int b, float rd){
		
		if ((a <= x+rd && a >= x-rd) && (b <= y+rd && b >= y-rd)) return true;
		else return false;
	}
	
	public void setSensor(int i, int j, int r, int tipo) {
		String nome;
		
		for(int k = -r; k <= r; k++){
			for(int l = -r; l <= r; l++){
				
				int x = i + k;
				int y = j + l;
				
				if((x >= 0 && x < 80 && y >= 0 && y < 80) && matrix[x][y].equals("t") && tipo == 1){ 
					
					matrix[x][y] = "v";
					nome = "" + x + "," + y;
					setChanged();
					notifyObservers(nome);
				}
				if((x >= 0 && x < 80 && y >= 0 && y < 80) && matrix[x][y].equals("v") && tipo == 0){ 
					
					matrix[x][y] = "t";
				 	nome = "" + x + "," + y;
				 	setChanged();
				 	notifyObservers(nome);
				}
			}
		}
		
		matrix[i][j] = "s";
		nome = "" + i + "," + j;
		setChanged();
		notifyObservers(nome);
	}
		
	public int lengthW() {return sizeW;}
	public int lengthH() {return sizeH;}
		
	public void setRow(String row[], int pos){
		for (int i = 0; i < sizeW && pos < sizeH; i++){
			matrix[pos][i] = new String(row[i]);
		}
	}
		
	// ler terreno do mapa
	public void loadTerrain(String name){
		Scanner scan = null; String linha = null;
		int i = 0;
		int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		
		try {
			scan = new Scanner(new FileReader(name));
			// deve-se usar o separador de linhas default da JVM
			scan.useDelimiter(System.getProperty("line.separator"));	
			linha = scan.next();
			Scanner lineScan = new Scanner(linha);
			lineScan.useDelimiter(",|\r");
			x1 = Integer.parseInt((lineScan.next()));start.setX(x1);
			y1 = Integer.parseInt((lineScan.next()));start.setY(y1);
			x2 = Integer.parseInt((lineScan.next()));end.setX(x2);
			y2 = Integer.parseInt((lineScan.next()));end.setY(y2);
			
			
			while(scan.hasNext()) {
				linha = scan.next();
				setRow(parseLine(linha),i);
				i++;
			}
			setCell(x1, y1, "i");
			setCell(x2, y2, "f");
		}
		catch(IOException ioExc) {System.out.println("Nao existe o ficheiro");}
		finally {scan.close();} // este bloco e sempre executado, haja erro ou nao !!
	}
	
	// auxiliar da leitura de terreno
	public static String[] parseLine(String line) {
		String type[] = new String[sizeW];
		int i = 0;
		Scanner lineScan = new Scanner(line);
		lineScan.useDelimiter(",|\r");

		while(lineScan.hasNext() && i < sizeW){
			type[i] = lineScan.next();
			i++;
		}	
	return type;
	}

	// Metodo para desenhar caminho no terreno
	public void setWay(Way w, long sp){
		int i, x1, x2, y1, y2;
		
		ArrayList<Point2D> trajectory = w.getTrajectory();
		for(i = 0; i < trajectory.size()-1; i++){
			
			x1 = trajectory.get(i).getX();
			y1 = trajectory.get(i).getY();
			
			x2 = trajectory.get(i+1).getX();
			y2 = trajectory.get(i+1).getY();
			
			// desenhar o caminho a partir de dois pontos 2D  
			drawLine(x1, y1, x2, y2, sp);
		}
	}
	
	// Metodo para desenhar caminho entre dois pontos
	public void drawLine(int xP, int yP, int xQ, int yQ, long speed){  
		int x = xP, y = yP, D = 0, HX = xQ - xP, HY = yQ - yP, c, M, xInc = 1, yInc = 1;
		
		if (HX < 0){xInc = -1; HX = -HX;}
	    if (HY < 0){yInc = -1; HY = -HY;}
	    if (HY <= HX){  
	    	c = 2 * HX; 
	    	M = 2 * HY;
	    	while(true){
	    		if(matrix[x][y].equals("w") || matrix[x][y].equals("c")) matrix[x][y] = "c";
	    		else if(!matrix[x][y].equals("s") && !matrix[x][y].equals("p")) matrix[x][y] = "a";
	    			
	    		String nome = "" + x + "," + y;
	    		setChanged();
	    		notifyObservers(nome);
	    		
	    		if (x == xQ) 
	    			break;
	    		x += xInc; 
	    		D += M;
	    		if (D > HX){
	    			y += yInc; 
	    			D -= c;
	    		}
	     	}
	    }
	    else{  
	    	c = 2 * HY; 
	    	M = 2 * HX;
	    	while(true){
	    		if(matrix[x][y].equals("w") || matrix[x][y].equals("c")) matrix[x][y] = "c";
	    		else if(!matrix[x][y].equals("s") && !matrix[x][y].equals("p")) matrix[x][y] = "a";
	    			
	    		String nome = "" + x + "," + y;
	    		setChanged();
	    		notifyObservers(nome);
	    		
	    		if (y == yQ) 
	    			break;
	    		y += yInc; 
	    		D += M;
	    		if (D > HY){
	    			x += xInc; 
	    			D -= c;
	    		}
	    	}
	    }
	}  
	
}