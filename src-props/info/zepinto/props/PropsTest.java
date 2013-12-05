package info.zepinto.props;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.LinkedHashMap;

public class PropsTest implements PropertyChangeListener {

	@Property
	int x = 23;
	
	@Property
	int y = 10;
	
	@Property
	Rectangle r = new Rectangle(10, 20);
	
	@Property
	File f = new File("/home/zp/Destop/x.tdt");
	
	public static void main(String[] args) throws Exception {
		PropsTest pt = new PropsTest();
		PropertyUtils.loadProperties(pt, new File("/home/zp/Desktop/pt.props"));
		LinkedHashMap<String, SerializableProperty> props = PropertyUtils.getProperties(pt, true); 
		System.out.println(props);
		PropertyUtils.setProperties(pt, props);
		PropertyUtils.saveProperties(pt, new File("/home/zp/Desktop/pt.props"));
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println(evt);
	}
}
