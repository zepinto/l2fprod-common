package info.zepinto.props;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;

public class PropsTest implements PropertyChangeListener {

	@Property
	int x = 23;
	
	@Property
	int y = 10;
	
	@Property
	Rectangle r = new Rectangle(10, 20);
	
	public static void main(String[] args) {
		PropsTest pt = new PropsTest();
		LinkedHashMap<String, SerializableProperty> props = PropertyUtils.getProperties(pt, true); 
		System.out.println(props);
		props.get("x").setValue(13);
		PropertyUtils.setProperties(pt, props);
		
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println(evt);
	}
}
