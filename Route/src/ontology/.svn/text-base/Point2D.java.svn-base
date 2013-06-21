package ontology;

import java.util.Random;

import terrain.Terrain;
import jade.content.Concept;


public class Point2D implements Concept{

	private int x;
	private int y;

	
	public Point2D() {
		x = 0;
		y = 0;
	}
	
	public Point2D(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public Point2D(Point2D pt){
		
		this.x = pt.getX();
		this.y = pt.getY();	
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point2D other = (Point2D) obj;
		if (x != other.getX())
			return false;
		if (y != other.getY())
			return false;
		return true;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void move(){
		x++;
	}
	
	public void moveRnd(){
		//massa das particulas atomicas
		final double Mt = 1.0;
		//massa das particulas brownianas
		double Mb = 0.3;
		//velocidade das paticulas atomicas
		double Vt = 1;
			//velocidade das particulas borwnianas
		double Vx = 10, Vy = 10;
		//numero aleatorio
		Random rnd = new Random();
		int newX=x,newY=y;

		double Ux, Uy, angulo, Wx, Wy;
		angulo = 2*Math.PI*rnd.nextDouble();
		Ux = Vt*Math.cos(angulo); //entre -1 e 1
		Uy = Vt*Math.sin(angulo); //entre -1 e 1
		Wx = Vx-Ux; //no maximo reduz uma unidade
		Wy = Vy-Uy; //no maximo reduz uma unidade
		angulo = 2*Math.PI*rnd.nextDouble();
		Vy += (Mt/(Mt+Mb))*(Wy*(Math.cos(angulo)-1)+Wx*Math.sin(angulo));
		Vx += (Mt/(Mt+Mb))*(Wx*(Math.cos(angulo)-1)-Wy*Math.sin(angulo));
		
		
		if(x+Vx < Terrain.sizeH && x+Vx > 0 && normalX(x,x+Vx) ) newX += Vx;
		else if((x-Vx < Terrain.sizeH && x-Vx > 0 && normalX(-x,-(x-Vx))) ) newX -= Vx; 
		if(y+Vy < Terrain.sizeW && y+Vy> 0 && normalY(y,y+Vy) ) newY += Vy;
		else if((y-Vy < Terrain.sizeW && y-Vy > 0 && normalY(-y,-(y-Vy))) ) newY -= Vy;
	
		if(impossiBro( newX, newY)==false) {x=newX;y=newY;}
	}
	
	
	public boolean normalX(double p2){
		
		boolean result=false;
		
		if((Terrain.sizeW/2)>40 && p2>0) result=true;
		if((Terrain.sizeW/2)<40 && p2<0) result =true;
		return result;
	}
	
	public boolean normalX(int p1,double p2){
		
		boolean result=false;
		
		if(Math.abs(Terrain.end.getX()-p1)>Math.abs(Terrain.end.getX()-p2)) result=true;
		
		return result;
	}
	
	public boolean normalY(int p1,double p2){
		
		boolean result=false;
		if(Math.abs(Terrain.end.getY()-p1)>Math.abs(Terrain.end.getY()-p2)) result=true;
		
		return result;
	}
	
	public Point2D clone(){
		return new Point2D(this);
	}
	
	
	public void rndMv2(){
		
		int rand;
        Random a= new Random();

        double xdir = 0;
        double ydir = 0;
        int xrand=0, yrand=0, randForce=-1;
        double force;
        double ang;
        double power;

        
            xdir=0;
            ydir=0;
                //campo gravitacionar atrai objecto para posição final
                power = 1000 / Math.pow(getRange(x, y, Terrain.sizeH, Terrain.sizeW), 2);
                ang = normaliseBearing(Math.PI / 2 - Math.atan2(y - Terrain.sizeH, x - Terrain.sizeW));
                xdir += Math.sin(ang) * power;
                ydir += Math.cos(ang) * power;

                //campo gravitacionar repele objecto random
                xrand = a.nextInt(50);
                yrand = a.nextInt(50);
                power = 1000 / Math.pow(getRange(x, y, xrand, yrand), 2);
                ang = normaliseBearing(Math.PI / 2 - Math.atan2(y - yrand, x - xrand));
                xdir += Math.sin(ang) * power;
                ydir += Math.cos(ang) * power;

                if(xdir <0 && x<Terrain.sizeW) x++;
                else if(xdir >0 && x>0) x--;
                if(ydir <0 && y>0) y--;
                else if(ydir >0 && y<Terrain.sizeH) y++;
                
        
    }
    public int getRange(int x1, int y1, int x2, int y2) {
        int xo = x2 - x1;
        int yo = y2 - y1;
        int h = (int) Math.sqrt(xo * xo + yo * yo);
        return h;
    }
    double normaliseBearing(double ang) {
        if (ang > Math.PI) {
            ang -= 2 * Math.PI;
        }
        if (ang < -Math.PI) {
            ang += 2 * Math.PI;
        }
        return ang;
    }
 
    // verifica se é possivel fazer este caminho sem ter zonas impossiveis pelo caminho
	public boolean impossiBro(int x2,int y2){
		//System.out.println(x + "," + y + "-ImpossiBro");
		boolean result=false;
		int x = this.x, y = this.y, D = 0, HX = x2 - x, HY = y2 - y, c, M, xInc = 1, yInc = 1;
		
		if (HX < 0){xInc = -1; HX = -HX;}
	    if (HY < 0){yInc = -1; HY = -HY;}
	    if (HY <= HX){  
	    	c = 2 * HX; 
	    	M = 2 * HY;
	    	while(true){
	    		if(Terrain.matrix[x][y].equals("p")) result=true;
	    		   		
	    		if (x == x2) 
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
	    		if(Terrain.matrix[x][y].equals("p")) result=true;
	    		
	    		if (y == y2) 
	    			break;
	    		y += yInc; 
	    		D += M;
	    		if (D > HY){
	    			x += xInc; 
	    			D -= c;
	    		}
	    	}
	    }
	return result;
	}
	
	public String toString(){
		StringBuilder s = new StringBuilder ();
		s.append("Pos x = "+y + " ");
		s.append("Pos y = "+x);
	return s.toString();
	}

}
