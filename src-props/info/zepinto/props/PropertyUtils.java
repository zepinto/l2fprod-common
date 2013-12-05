package info.zepinto.props;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetDialog;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.swing.BannerPanel;

public class PropertyUtils {

	private static SerializableProperty createProperty(Object obj, Field f,
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
		setProperties(obj, props, true);
	}

	public static void setProperties(Object obj,
			LinkedHashMap<String, SerializableProperty> props,
			boolean triggerEvents) {
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

				try {
					f.set(obj, propertyValue);
				} catch (IllegalArgumentException e) {
					try {
						if ("int".equalsIgnoreCase(f.getGenericType()
								.toString())
								|| "Integer".equalsIgnoreCase(f
										.getGenericType().toString())) {
							String className = propertyValue.getClass()
									.toString();
							if (className.equals("java.lang.String")) {
								propertyValue = Integer
										.parseInt((String) propertyValue);
								f.set(obj, propertyValue);
							} else if (className.equals("java.lang.Long")) {
								propertyValue = Integer
										.valueOf(((Long) propertyValue)
												.intValue());
								f.set(obj, propertyValue);
							}
						} else if ("short".equalsIgnoreCase(f.getGenericType()
								.toString())) {
							propertyValue = Short
									.valueOf(((Long) propertyValue)
											.shortValue());
							f.set(obj, propertyValue);
						} else if ("byte".equalsIgnoreCase(f.getGenericType()
								.toString())) {
							propertyValue = Byte.valueOf(((Long) propertyValue)
									.byteValue());
							f.set(obj, propertyValue);
						} else if ("float".equalsIgnoreCase(f.getGenericType()
								.toString())) {
							propertyValue = Float
									.valueOf(((Double) propertyValue)
											.floatValue());
							f.set(obj, propertyValue);
						} else {
							f.set(obj, propertyValue);
						}
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}

				if (triggerEvents && !oldValue.equals(propertyValue)
						&& obj instanceof PropertyChangeListener)
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

	public static void saveProperties(Object obj, File f) throws IOException {
		LinkedHashMap<String, SerializableProperty> props = getProperties(obj,
				true);
		Properties p = new Properties();
		for (SerializableProperty prop : props.values())
			p.setProperty(prop.getName(), prop.toString());

		p.store(new FileWriter(f), "Properties saved on " + new Date());
	}

	public static void loadProperties(Object obj, File f) throws IOException {
		loadProperties(obj, f, true);
	}

	public static void loadProperties(Object obj, File f, boolean triggerEvents)
			throws IOException {
		Properties p = new Properties();
		p.load(new FileReader(f));
		LinkedHashMap<String, SerializableProperty> props = getProperties(obj,
				true);
		for (Entry<Object, Object> entry : p.entrySet()) {
			if (props.containsKey(entry.getKey())) {
				SerializableProperty sp = props.get(entry.getKey());
				sp.fromString("" + entry.getValue());
			}
		}

		setProperties(obj, props, triggerEvents);
	}

	public static void editProperties(Window parent, Object obj, boolean editable) {

		final PropertySheetPanel psp = new PropertySheetPanel();
		//psp.setEditorFactory(new PropertyEditorRegistry());
		//psp.setRendererFactory(new PropertyRendererRegistry());
		psp.setMode(PropertySheet.VIEW_AS_CATEGORIES);
		psp.setToolBarVisible(false);
		psp.setEnabled(true);
		psp.setSortingCategories(true);
		psp.setDescriptionVisible(true);
		Collection<SerializableProperty> props = getProperties(obj, editable)
				.values();

		//if (!editable)
			for (DefaultProperty p : props)
				p.setEditable(editable);

		for (SerializableProperty p : props)
			psp.addProperty(p);

		final PropertySheetDialog propertySheetDialog = createWindow(parent, editable, psp,
				"Properties of "+obj.getClass());

		if (!propertySheetDialog.ask()) {
			System.out.println("cancelled");
			return;
		}
		
		LinkedHashMap<String, SerializableProperty> newProps = new LinkedHashMap<>();
		for (com.l2fprod.common.propertysheet.Property p : psp.getProperties())
			newProps.put(p.getName(), new SerializableProperty(p));

		setProperties(obj, newProps, true);      

	}

	public static PropertySheetDialog createWindow(Window parent,
			boolean editable, final PropertySheetPanel psp, String title) {
		final PropertySheetDialog propertySheetDialog;
		if (parent instanceof Dialog) {
			Dialog pDialog = (Dialog) parent;
			propertySheetDialog = new PropertySheetDialog(pDialog);
		} else if (parent instanceof Frame) {
			Frame pFrame = (Frame) parent;
			propertySheetDialog = new PropertySheetDialog(pFrame);
		} else {
			propertySheetDialog = new PropertySheetDialog() {
				private static final long serialVersionUID = 1L;

				@Override
				public void ok() {
					if (psp.getTable().getEditorComponent() != null)
						psp.getTable().commitEditing();
					super.ok();
				};
			};
		}
		if (editable) {
			propertySheetDialog
			.setDialogMode(PropertySheetDialog.OK_CANCEL_DIALOG);
		} else {
			propertySheetDialog.setDialogMode(PropertySheetDialog.CLOSE_DIALOG);
		}

		int sb = 1;
		for (Component compL0 : propertySheetDialog.getRootPane()
				.getComponents()) {
			if (compL0 instanceof JLayeredPane) {
				for (Component compL01 : ((JLayeredPane) compL0)
						.getComponents()) {
					if (!(compL01 instanceof JPanel))
						continue;
					for (Component compL1 : ((JPanel) compL01).getComponents()) {
						if (compL1 instanceof BannerPanel)
							continue;
						if (compL1 instanceof JPanel) {
							for (Component compL2 : ((JPanel) compL1)
									.getComponents()) {
								for (Component compL3 : ((JPanel) compL2)
										.getComponents()) {
									if (compL3 instanceof JButton) {
										if (propertySheetDialog.getDialogMode() == PropertySheetDialog.OK_CANCEL_DIALOG
												&& sb == 1) {
											((JButton) compL3).setText("OK");
											sb--;
										} else if (propertySheetDialog
												.getDialogMode() == PropertySheetDialog.CLOSE_DIALOG
												|| sb == 0) {
											((JButton) compL3)
											.setText(propertySheetDialog
													.getDialogMode() == PropertySheetDialog.CLOSE_DIALOG ? "Close"
															: "Cancel");
											sb--;
											break;
										}
										if (sb < 0)
											break;
									}
								}
								if (sb < 0)
									break;
							}
						}
					}
				}
			}
		}

		if (title != null) {
			propertySheetDialog.getBanner().setTitle(title);
			propertySheetDialog.setTitle(title);
		}
		//propertySheetDialog.setIconImage(ImageUtils.getImage("images/menus/settings.png"));
		//propertySheetDialog.getBanner().setIcon(ImageUtils.getIcon("images/settings.png"));
		propertySheetDialog.getContentPane().add(psp);
		propertySheetDialog.pack();

		propertySheetDialog.setLocationRelativeTo(null);
		propertySheetDialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		return propertySheetDialog;
	}
}