package info.zepinto.props;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Date;

public class PropsTest implements PropertyChangeListener {

	@Property
	Integer x = 23;
	
	@Property
	Integer y = 10;
	
	@Property
	Rectangle r = new Rectangle(10, 20);
	
	@Property
	File f = new File("/home/zp/Destop/x.tdt");
	
	@Property
	Date date = new Date();
	
	public static void main(String[] args) throws Exception {
		PropsTest pt = new PropsTest();
		if (new File("/home/zp/Desktop/pt.props").canRead())
			PropertyUtils.loadProperties(pt, new File("/home/zp/Desktop/pt.props"), false);
		PropertyUtils.editProperties(null, pt, true);
		PropertyUtils.saveProperties(pt, new File("/home/zp/Desktop/pt.props"));
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println(evt);
	}
}
