package me.bomb.permissionattributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.attribute.Attribute;


import static me.bomb.permissionattributes.PermissionAttributes.DEFAULT;

public final class Configuration {
	
	private final HashMap<String, EnumMap<Attribute, Double>> attributes;
	private final String[] ids;
	
	public Configuration(HashMap<String, EnumMap<Attribute, Double>> attributes, String[] ids) {
		HashMap<String, EnumMap<Attribute, Double>> newattributes = new HashMap<>(attributes.size());
		for(Entry<String, EnumMap<Attribute, Double>> entry : attributes.entrySet()) {
			newattributes.put(entry.getKey(),entry.getValue().clone());
		}
		this.attributes = newattributes;
		this.ids = ids.clone();
	}
	
	protected Configuration(Logger logger, Path file) {
		final HashMap<String, EnumMap<Attribute, Double>> attributes = new HashMap<String, EnumMap<Attribute, Double>>();
		final SimpleConfiguration sc;
		{
			byte[] bytes = null;
			InputStream is = null;
			FileSystem fs = file.getFileSystem();
			FileSystemProvider fsp = fs.provider();
			try {
				BasicFileAttributes fileattributes = fsp.readAttributes(file, BasicFileAttributes.class);
				is = fsp.newInputStream(file);
				long filesize = fileattributes.size();
				if(filesize > 0x00100000) {
					filesize = 0x00100000;
				}
				bytes = new byte[(int)filesize];
				int size = is.read(bytes);
				if(size < filesize) {
					bytes = Arrays.copyOf(bytes, size);
				}
				is.close();
			} catch (IOException e1) {
				if(is != null) {
					try {
						is.close();
					} catch (IOException e2) {
					}
				}
				try {
					is = Configuration.class.getClassLoader().getResourceAsStream("config.yml");
					bytes = new byte[0x1000];
					bytes = Arrays.copyOf(bytes, is.read(bytes));
					is.close();
					OutputStream os = null;
					try {
						os = fsp.newOutputStream(file);
						os.write(bytes);
						os.close();
					} catch (IOException e3) {
						if(os != null) {
							try {
								os.close();
							} catch (IOException e4) {
							}
						}
					}
				} catch (IOException e3) {
					if(is != null) {
						try {
							is.close();
						} catch (IOException e4) {
						}
					}
				}
			}
			if(bytes == null) {
				this.attributes = attributes;
				this.ids = new String[0];
				return;
			}
			sc = new SimpleConfiguration(bytes, StandardCharsets.US_ASCII);
		}
		final String attributesidkey = "attributes\0";
		final String[] keys = sc.getSubKeys(attributesidkey);
		int i = keys.length;
		if(i > 255) {
			i = 255;
		}
		StringBuilder sb = new StringBuilder("\nInvalid options:");
		HashMap<Byte, String> priorities = new HashMap<Byte, String>(i);
		String[] ids = new String[i];
		while(--i > -1) {
			final String attributeid = keys[i], key0 = attributesidkey.concat(attributeid);
			attributes.put(attributeid, readAttributes(sb, sc, key0.concat("\0")));
			if(attributeid.equals(DEFAULT)) {
				priorities.put((byte)0, DEFAULT);
				continue;
			}
			byte priority = sc.getByteOrDefault(key0, (byte)0x01);
			byte j = 0;
			while(priorities.containsKey(priority) && --j != 0) {
				++priority;
			}
			priorities.put(priority, attributeid);
		}
		i = ids.length;
		byte j = -1;
		while(i > -1 && --j != -1) {
			String attributeid = priorities.get(j);
			if(attributeid == null) {
				continue;
			}
			ids[--i] = attributeid;
		}
		if(sb.length() > 17 && logger != null) {
			logger.warning(sb.toString());
		}
		sb = new StringBuilder("\nAvilable attributes:");
		Attribute[] values = Attribute.values();
		i = values.length;
		while(--i > -1) {
			sb.append(' ');
			sb.append(values[i].name());
		}
		logger.info(sb.toString());
		this.attributes = attributes;
		this.ids = ids;
	}
	
	public HashMap<String, EnumMap<Attribute, Double>> getClone() {
		HashMap<String, EnumMap<Attribute, Double>> attributes = new HashMap<>(this.attributes.size());
		for(Entry<String, EnumMap<Attribute, Double>> entry : this.attributes.entrySet()) {
			attributes.put(entry.getKey(),entry.getValue().clone());
		}
		return attributes;
	}
	protected HashMap<String, EnumMap<Attribute, Double>> get() {
		return this.attributes;
	}
	
	public EnumMap<Attribute, Double> getAttributesClone(final String id) {
		final EnumMap<Attribute, Double> enummap;
		if(id == null || (enummap = attributes.get(id)) == null) {
			return null;
		}
		return enummap.clone();
	}
	protected EnumMap<Attribute, Double> getAttributes(final String id) {
		final EnumMap<Attribute, Double> enummap;
		if(id == null || (enummap = attributes.get(id)) == null) {
			return null;
		}
		return enummap;
	}
	
	public Double getAttributeOption(final String id, final Attribute attribute) {
		final EnumMap<Attribute, Double> enummap;
		final Double value;
		if(id == null || attribute == null || (enummap = attributes.get(id)) == null || (value = enummap.get(attribute)) == null) {
			return Double.NaN;
		}
		return value;
	}
	
	protected String[] getIds() {
		return this.ids;
	}
	
	private static final EnumMap<Attribute, Double> readAttributes(final StringBuilder errors, final SimpleConfiguration sc, final String key) {
		final String[] keys = sc.getSubKeys(key);
		int i = keys.length;
		EnumMap<Attribute, Double> attributes = new EnumMap<>(Attribute.class);
		while(--i > -1) {
			final String attributename = keys[i], key0 = key.concat(attributename);
			final Attribute attribute;
			try {
				attribute = Attribute.valueOf(attributename);
			} catch (IllegalArgumentException e) {
				errors.append(' ');
				errors.append(key.replace('\0', '.'));
				continue;
			}
			Double value;
			if(attribute == null || (value = sc.getDoubleOrDefault(key0, Double.NaN)) == Double.NaN) {
				continue;
			}
			attributes.put(attribute, value);
		}
		return attributes;
	}

}
