package info.zepinto.props;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.xml.bind.DatatypeConverter;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.util.converter.Converter;
import com.l2fprod.common.util.converter.ConverterRegistry;

public class SerializableProperty extends DefaultProperty {

	private static final long serialVersionUID = 2246989350132202842L;

	public SerializableProperty() {
		super();
	}
	
	public SerializableProperty(Property prop) {
		setName(prop.getName());
		setType(prop.getClass());
		setDisplayName(prop.getDisplayName());
		setCategory(prop.getCategory());
		setValue(prop.getValue());
		setEditable(prop.isEditable());
		setShortDescription(prop.getShortDescription());
	}

	public SerializableProperty(String name, Class<?> clazz, Object value) {
		setName(name);
		setDisplayName(name);
		setShortDescription(name);		
		setType(clazz);
		setValue(value);
	}

	@Override
	public String toString() {
		Converter conv = ConverterRegistry.instance().getConverter(getValue().getClass(), String.class);
		if (conv == null) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream dos = new ObjectOutputStream(out);
				dos.writeObject(getValue());
				byte[] data = out.toByteArray();
				return DatatypeConverter.printBase64Binary(data);
			}
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}
		else {
			return conv.convert(String.class, getValue())+ "";
		}		
	}

	public void fromString(String text) {
		Converter conv = ConverterRegistry.instance().getConverter(String.class, getValue().getClass());
		if (conv == null) {
			try {
				byte[] data = DatatypeConverter.parseBase64Binary(text);
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
				setValue(ois.readObject());
			}
			catch (Exception e) {
				e.printStackTrace();
				setValue(null);
			}
		}
		else {
			setValue(conv.convert(getValue().getClass(), text));
		}
	}
	
    /**
     * Convenience method to create a default property object from various parameters
     * @param propertyName The name of the property
     * @param propertyClass The class of the values of this property
     * @param value The current value for this property
     * @param isEditable Sets whether the property can be edited by the user
     * @return A DefaultProperty object @see DefaultProperty
     */
    public static SerializableProperty getPropertyInstance(String propertyName, Class<?> propertyClass, Object value, boolean isEditable) {
        
    	SerializableProperty property = new SerializableProperty();
        
    	property.setName(propertyName);
        property.setDisplayName(propertyName);
        property.setType(propertyClass);
        property.setValue(value);
        property.setEditable(isEditable);
        
        return property;
    }
	
	public static void main(String[] args) {
		SerializableProperty p = getPropertyInstance("test", Color.class, Color.red, true);
		String s = p.toString();
		p = getPropertyInstance("test3", Color.class, Color.blue, true);
		p.fromString(s);
		System.out.println(p.getValue());
		System.out.println(p);
		p = getPropertyInstance("test2", Double.class, 12.23, true);
		System.out.println(p.toString());
	}
}