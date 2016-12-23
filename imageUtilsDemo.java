package Disparity;

/*Image Disparity Lab */
/*Programmed by A-Shawni Mitchell December 5,2016 */

//Basic operations for manipulating image files

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class imageUtilsDemo{	

	public static void displayImage(BufferedImage img) {
		ImageIcon icon=new ImageIcon(img);
		JFrame frame=new JFrame();
		frame.setLayout(new FlowLayout());
		frame.setSize(img.getWidth() + 100  ,img.getHeight() + 100);
		JLabel lbl=new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void displayImage(int[][][] matrix){
		displayImage(convert3DMatrixToBufferedImage(matrix));
	}
   
	public static BufferedImage convert3DMatrixToBufferedImage(int[][][] matrix)	{
		BufferedImage img = new BufferedImage(matrix[0].length, matrix.length, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < img.getHeight(); i++){
			for (int j = 0; j < img.getWidth(); j++){
				img.setRGB(j, i, new Color(matrix[i][j][0], matrix[i][j][1], matrix[i][j][2]).getRGB());
			}
		}
		return img;
	}

	public static BufferedImage loadImageAsBufferedImage(String path)  	{
		BufferedImage img = null;
		try {
		    img = ImageIO.read(new File(path));
		} catch (IOException e) {}
		return img;
	}

	public static int[][][] convertBufferedImageTo3DMatrix(BufferedImage img)	{
		int[][][] matrix = new int[img.getHeight()][img.getWidth()][3];
		
		for (int i = 0; i < img.getHeight(); i++)		{
			for (int j = 0; j < img.getWidth(); j++)			{
				Color color = new Color(img.getRGB(j,i));
				matrix[i][j][0] = color.getRed();
				matrix[i][j][1] = color.getGreen();
				matrix[i][j][2] = color.getBlue();
			}
		}
		return matrix;
	}
	
	public static int[][][] loadImageAs3DMatrix(String path)	{
		return convertBufferedImageTo3DMatrix(loadImageAsBufferedImage(path));
	}	
    
	public static int[][][] grayToNormalizedColor(int [][] I){   
		int [][][] G = new int[I.length][I[0].length][3];
		int max=0;
		int min = 10000;
		int color;
		for(int r=0;r<I.length;r++)
			for(int c=0;c<I[0].length;c++)
				if(I[r][c] > max)
					max = I[r][c];
				else
					if (I[r][c] < min)
						min = I[r][c];      
		for(int r=0;r<I.length;r++)
			for(int c=0;c<I[0].length;c++){
				color = 255*(I[r][c] - min)/(max-min);
				for(int k=0;k<3;k++)
					G[r][c][k]=color;
			}
		return G;
	}
	
	/*Get Image Disparity Method */
	public static int[][] getImageDisparity(int[][][] IL, int[][][] IR, int occlusionCost) {
		
		int[][] disparity = new int[IL.length][IL[0].length];
		
		for(int i=0;i<IL.length;i++)
			disparity[i] = getRowDisparity(IL[i],IR[i],occlusionCost);
		return disparity;
	}
	
	/*Get Row Disparity Method */
	public static int[] getRowDisparity(int[][] L, int[][] R, int occlusionCost) {
		
		int[][] d = new int[L.length+1][R.length+1];
		
		char[][] direction = new char[L.length+1][R.length+1];
		
		for(int i=0;i<L.length;i++) { //Populate of Matrix with corresponding cost values
			d[i][0] = d[0][i] = i*occlusionCost;
			direction[i][0] = 'U';
			direction[0][i] = 'L';
		}
		
		
		
		for(int j=0;j<R.length;j++)
			for(int i=0;i<L.length;i++) {
				
				//store the right value in a,b,c
				int a = d[i][j+1] + occlusionCost; //up
				int b = d[i+1][j] + occlusionCost; //left
				
				//Pythagorus Theorem
				int sum = 0;
				
				for(int k = 0;k <=2 ;k++)
					sum += (L[i][k]-R[j][k])*(L[i][k]-R[j][k]);
				
				int c = (int) (d[i][j] +Math.sqrt(sum)); //diagonal
				
				int min = min(a,b,c); //min value
				
				d[i+1][j+1] = min; //store min
				
				//Store min value in direction matrix
				if(min==a)
					direction[i+1][j+1]='U';
				if(min == b)
					direction[i+1][j+1]='L';
				if(min==c)
					direction[i+1][j+1]='D';
			}
		
		int[] S = new int[L.length];
		int i = L.length-1;
		int j = R.length-1;
		
		while(i>0 && j>0) {
			
			S[i-1] = (i-1) - (j-1);
			
			switch(direction[i][j]) {
			
			case 'U': //Move up in matrix
				i--;
				break;
				
			case 'L': //Move left
				j--;
				break;
				
			case 'D': //Move up and left
				i--;
				j--;
				break;
				
			}
			
		}
		
		return S;
	}
	
	/* Min Method */
	public static int min(int a, int b, int c) {
		if (a<b)
			b=a;
		if (b<c)
			return b;
		return c;
	}
 
	/*Main Method */
	public static void main(String[] args) {	
		int [][][] L = loadImageAs3DMatrix("/Users/shawnimitch/Documents/School/Eclipse/Lab8/src/Disparity/im2L.png");  
		int [][][] R = loadImageAs3DMatrix("/Users/shawnimitch/Documents/School/Eclipse/Lab8/src/Disparity/im2R.png"); 
		
		int[][] disparity = getImageDisparity(L,R,20);
		int[][][] normalizedDisparity = grayToNormalizedColor(disparity);

		displayImage(normalizedDisparity);
		//displayImage(L);   
		//displayImage(R);   
	}
}
