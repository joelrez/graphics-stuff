import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.imageio.ImageIO;

public class viewer {
	public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // slider to control horizontal rotation
        JSlider headingSlider = new JSlider(-180, 180, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // slider to control vertical rotation
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        // panel to display render results
        JPanel renderPanel = new JPanel() {
                public void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Color.BLACK);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    
                    List<Triangle> tris = new ArrayList<Triangle>();
                    tris.add(new Triangle(new Vertex(100, 75, 100),
                                          new Vertex(50, 200, 100),
                                          new Vertex(150, 200, -50),
                                          Color.BLUE));
                    tris.add(new Triangle(new Vertex(100, 100, 0),
			                              new Vertex(50, 175, 0),
			                              new Vertex(150, 175, 0),
                                          Color.RED));
                    
                    double heading = Math.toRadians(headingSlider.getValue());
                    Matrix3 headingTransform = new Matrix3(new double[] {
                            Math.cos(heading), 0, Math.sin(heading),
                            0, 1, 0,
                            -Math.sin(heading), 0, Math.cos(heading)
                        });
                    double pitch = Math.toRadians(pitchSlider.getValue());
                    Matrix3 pitchTransform = new Matrix3(new double[] {
                            1, 0, 0,
                            0, Math.cos(pitch), Math.sin(pitch),
                            0, -Math.sin(pitch), Math.cos(pitch)
                        });
                    
                    Vertex centroid = new Vertex((tris.get(0).centroid.x +tris.get(1).centroid.x)/2,
                    							 (tris.get(0).centroid.y + tris.get(1).centroid.y)/2,
                    							 (tris.get(0).centroid.z + tris.get(1).centroid.z)/2);
                    
                    Matrix3 transform = headingTransform.multiply(pitchTransform);
                    
                    BufferedImage img = 
                    	    new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                    
                    double [] depthMap = new double[getWidth()*getHeight()];
                    
                    for (int i = 0; i < depthMap.length; i++) 
                    	depthMap[i] = Double.NEGATIVE_INFINITY;
                    
                    for (Triangle t:tris) {
                    	Vertex v1 = transform.transform(t.v1.subtract(centroid));
                    	Vertex v2 = transform.transform(t.v2.subtract(centroid));
                    	Vertex v3 = transform.transform(t.v3.subtract(centroid));
                    	v1 = v1.add(centroid);
                    	v2 = v2.add(centroid);
                    	v3 = v3.add(centroid);
                    	
                    	Triangle t2 = new Triangle(v1,v2,v3,t.color);
                    	
                    	for (int x = 0; x < getWidth(); x++) {
                    		for (int y = 0; y < getHeight(); y++) {
                    			double depth = t2.getDepth(x, y);
                    			int index = y*getWidth() + x;
                    			if (t2.isInside(x,y) && depthMap[index] < depth) {
                    				depthMap[index] = depth;
                    				img.setRGB(x, y, t2.color.getRGB());
                    			}
                    		}
                    	}
                    }
                    
                    g2.drawImage(img, 0, 0, null);
                }
            };
	    
        headingSlider.addChangeListener(e -> renderPanel.repaint());
	    pitchSlider.addChangeListener(e -> renderPanel.repaint());
        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}
class Vertex {
    double x;
    double y;
    double z;
    Vertex(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    Vertex subtract(Vertex v) {
    	return new Vertex(this.x - v.x, this.y - v.y, this.z - v.z);
    }
    Vertex add(Vertex v) {
    	return new Vertex(this.x + v.x, this.y + v.y, this.z + v.z);
    }
}

class Triangle{
    Vertex v1;
    Vertex v2;
    Vertex v3;
    Color color;
    Vertex centroid;
    
    Triangle(Vertex v1, Vertex v2, Vertex v3, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
        this.getCentroid();
    }
    
    boolean isInside(double ... X) {
		Vertex[] v = {this.v1,this.v2,this.v3};
		for (int i = 0; i < v.length; i++) {
			Vertex A = v[i];
			Vertex B = v[(i+1)%v.length];
			Vertex C = v[(i+2)%v.length];
			double det = (A.x-B.x)*(C.y - X[1]) - (A.y - B.y)*(C.x - X[0]);
			if (det == 0)
				return false;
			double t = ((B.y - A.y)*(A.x-X[0])+(A.x - B.x)*(A.y-X[1]))/det;
			if (t<1 && t>0)
				return false;
		}
		return true;
	}
    
    double getDepth(double x, double y) {
    	Vertex A = this.v1;
    	Vertex B = this.v2;
    	Vertex C = this.v3;
    	
    	double det = (B.x - A.x)*(C.y - A.y) - (C.x - A.x)*(B.y - A.y);
    	
    	double s = ((C.y - A.y)*(x - A.x) + (A.x-C.x)*(y - A.y))/det;
    	double t = ((A.y - B.y)*(x - A.x) + (B.x - A.x)*(y - A.y))/det;
    	
    	return (1-s-t)*A.z + t*C.z + s*B.z;
    }
    
    private void getCentroid() {
    	// computing scalar s
    	Vertex v3prime = new Vertex((this.v1.x+this.v2.x)/2,
    								(this.v1.y+this.v2.y)/2,
    								(this.v1.z+this.v2.z)/2);
    	Vertex v2prime = new Vertex((this.v1.x+this.v3.x)/2, 
    								(this.v1.y+this.v3.y)/2,
    								(this.v1.z+this.v3.z)/2);
    	double det = (v3prime.x-v3.x)*(this.v2.y-v2prime.y) - (this.v2.x-v2prime.x)*(v3prime.y-this.v3.y);
    	double s = ((this.v2.y-v2prime.y)*(this.v3.x-this.v2.x) + (v2prime.x-this.v2.x)*(this.v3.y-this.v2.y))/det;
    	
    	// find centroid
    	double x = this.v3.x + s*(this.v3.x - v3prime.x);
    	double y = this.v3.y + s*(this.v3.y - v3prime.y);
    	this.centroid = new Vertex(x,y, this.getDepth(x, y)); 
    }
}

class Matrix3 {
    double[] values;
    Matrix3(double[] values) {
        this.values = values;
    }
    Matrix3 multiply(Matrix3 other) {
        double[] result = new double[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                for (int i = 0; i < 3; i++) {
                    result[row * 3 + col] +=
                        this.values[row * 3 + i] * other.values[i * 3 + col];
                }
            }
        }
        return new Matrix3(result);
    }
    Vertex transform(Vertex in) {
        return new Vertex(
            in.x * values[0] + in.y * values[3] + in.z * values[6],
            in.x * values[1] + in.y * values[4] + in.z * values[7],
            in.x * values[2] + in.y * values[5] + in.z * values[8]
        );
    }
}