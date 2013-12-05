package info.zepinto.props;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyUtils {

	public static SerializableProperty createProperty(Object obj, Field f,
			boolean forEdit) {
		Property a = f.getAnnotation(Property.class);

		if (a != null) {

			f.setAccessible(true);
			String name = a.name();
			String desc = a.description();
			Class<? extends PropertyEditor> editClass = null;
			String category = a.category();

			if (a.name().length() == 0) {
				name = f.getName();
			}
			if (a.description().length() == 0) {
				desc = f.getName();
			}

			if (a.editorClass() != PropertyEditor.class) {
				editClass = a.editorClass();
			}

			if (category == null || category.length() == 0) {
				category = "Base";
			}

			Object o = null;
			try {
				o = f.get(null);
			} catch (Exception e) {
				// nothing
			}
			if (o == null) {
				try {
					o = f.get(obj);
				} catch (Exception e) {
					// nothing
				}
			}

			SerializableProperty pp = new SerializableProperty(name,
					f.getType(), o);
			pp.setShortDescription(desc);
			pp.setEditable(!a.editable());
			pp.setDisplayName(name);
			pp.setEditor(editClass);
			if (category != null && category.length() > 0) {
				pp.setCategory(category);
			}
			return pp;
		}
		return null;
	}

	private static Field[] getFields(Object o) {
		Class<?> c;
		if (o instanceof Class<?>)
			c = (Class<?>) o;
		else
			c = o.getClass();

		HashSet<Field> fields = new HashSet<>();
		for (Field f : c.getFields())
			fields.add(f);
		for (Field f : c.getDeclaredFields()) {
			f.setAccessible(true);
			fields.add(f);
		}
		return fields.toArray(new Field[0]);
	}

	public static LinkedHashMap<String, SerializableProperty> getProperties(
			Object obj, boolean editable) {
		LinkedHashMap<String, SerializableProperty> props = new LinkedHashMap<String, SerializableProperty>();

		for (Field f : getFields(obj)) {
			SerializableProperty pp = createProperty(obj, f, editable);
			if (pp != null)
				props.put(f.getName(), pp);
		}
		return props;
	}

	public static void setProperties(Object obj,
			LinkedHashMap<String, SerializableProperty> props) {
		Class<? extends Object> providerClass = obj instanceof Class<?> ? (Class<?>) obj
				: obj.getClass();

		String name;
		SerializableProperty property;
		Object propertyValue;
		for (Field f : getFields(providerClass)) {
			Property a = f.getAnnotation(Property.class);
			if (a == null)
				continue;

			if (a.name().isEmpty())
				name = f.getName();
			else
				name = a.name();

			property = props.get(name);
			if (property == null) {
				Logger.getGlobal().log(Level.WARNING,
						"Property " + name + " will not be saved.");
				continue;
			}
			try {
				propertyValue = property.getValue();
				Object oldValue = f.get(obj);

				if (propertyValue.getClass() == f.getType())
					f.set(obj, propertyValue);
				else {
					
					try {
                        if ("int".equalsIgnoreCase(f.getGenericType().toString())
                                || "Integer".equalsIgnoreCase(f.getGenericType().toString())) {
                            String className = propertyValue.getClass().toString();
                            if(className.equals("java.lang.String")){
                            	propertyValue = Integer.parseInt((String) propertyValue);
                            	f.set(obj, propertyValue);
                            }
                            else if (className.equals("java.lang.Long")) {
                            	propertyValue = Integer.valueOf(((Long) propertyValue).intValue());
                                f.set(obj, propertyValue);
                            }
                        }
                        else if ("short".equalsIgnoreCase(f.getGenericType().toString())) {
                        	propertyValue = Short.valueOf(((Long) propertyValue).shortValue());
                            f.set(obj, propertyValue);
                        }
                        else if ("byte".equalsIgnoreCase(f.getGenericType().toString())) {
                        	propertyValue = Byte.valueOf(((Long) propertyValue).byteValue());
                            f.set(obj, propertyValue);
                        }
                        else if ("float".equalsIgnoreCase(f.getGenericType().toString())) {
                        	propertyValue = Float.valueOf(((Double) propertyValue).floatValue());
                            f.set(obj, propertyValue);
                        }
                    }
                    catch (Exception e2) {
                        e2.printStackTrace();
                    }					
				}

				if (!oldValue.equals(propertyValue) && obj instanceof PropertyChangeListener)
					((PropertyChangeListener) obj)
							.propertyChange(new PropertyChangeEvent(
									PropertyUtils.class, f.getName(), oldValue,
									propertyValue));
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}
}
