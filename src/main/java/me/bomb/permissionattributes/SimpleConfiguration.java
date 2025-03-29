package me.bomb.permissionattributes;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/*
 * Read only configuration
 */
final class SimpleConfiguration {
	
	protected final HashMap<String,String> kv;
	
	protected SimpleConfiguration(byte[] bytes, Charset charset) {
		char[] chars = new String(bytes, charset).toCharArray();
		HashMap<String,String> kv = new HashMap<>();
		int i = chars.length;
		byte level = 0, maxlevel = 0;
		ArrayList<Integer> intlist = new ArrayList<Integer>();
		while(--i>-1) {
			char ch = chars[i];
			if(ch == 0x0000000A) {
				++level;
				if(maxlevel<level) maxlevel = level;
				int tadd = ((i << 8) | level);
				intlist.add(tadd);
				level = 0;
			} else if(ch == 0x00000020) {
				++level;
			} else {
				level = 0;
			}
		}
		if(maxlevel<level) maxlevel = level;
		++maxlevel;
		i = intlist.size();
		byte[] levels = new byte[i];
		char[][] strs = new char[i][];
		int pstrid = 0;
		while(--i>-1) {
			int strid = intlist.get(i);
			levels[i] = level;
			int strsize = -level;
			level = (byte) strid;
			strid>>=8;
			strsize+=strid;
			strsize-=pstrid;
			char[] strbytes = new char[strsize];
			pstrid = strid;
			while(--strsize>-1) {
				strbytes[strsize] = chars[--strid];
			}
			strs[i] = strbytes;
		}
		i = strs.length;
		byte plevel = 0;
		String[] pkey = new String[maxlevel];
		++levels[i-1];
		while(--i>-1) {
			level = levels[i];
			char[] str = strs[i];
			int valuekeysplit = 0;
			while(valuekeysplit<str.length) {
				if(str[valuekeysplit] == 0x0000003A) {
					break;
				}
				++valuekeysplit;
			}
			if(str.length == 0 || str[0] == 0x00000023) continue;
			if(valuekeysplit != str.length) {
				String key = new String(Arrays.copyOf(str, valuekeysplit));
				StringBuilder fullkey = new StringBuilder();
				if(level>plevel) {
					pkey[level] = key;
					plevel = level;
				} else if(level<plevel) {
					pkey[level] = key;
					for(byte k = plevel;level<k;--k) {
						pkey[k] = null;
					}
					plevel = level;
				} else {
					pkey[level] = key;
				}
				++level;
				for(byte k = 1; k < level;++k) {
					String apk = pkey[k];
					if(apk==null) continue;
					fullkey.append('\0');
					fullkey.append(apk);
				}
				fullkey.delete(0, 1);
				++valuekeysplit;
				boolean hasvalue = valuekeysplit < str.length && str[valuekeysplit] == 0x00000020;
				String value = hasvalue&&++valuekeysplit != str.length ? new String(Arrays.copyOfRange(str, valuekeysplit, str.length)) : "";
				kv.put(fullkey.toString(), value);
			}
		}
		this.kv = kv;
	}
	
	public boolean hasKey(String key) {
		return kv.containsKey(key);
	}
	
	public double getDoubleOrDefault(String key, double defaultvalue) {
		String value = kv.get(key);
		if(value==null) return defaultvalue;
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return defaultvalue;
		}
	}
	
	public byte getByteOrDefault(String key, byte defaultvalue) {
		String value = kv.get(key);
		if(value==null) return defaultvalue;
		char[] hexbyte = value.toCharArray();
		if(hexbyte.length == 2) {
			byte v1 = hexCharToByte(hexbyte[1]);
			byte v2 = hexCharToByte(hexbyte[0]);
			if(v1 == -1 || v2 == -1) {
				return defaultvalue;
			}
			v1 |= v2 << 4;
			return v1;
		} else if(hexbyte.length == 1) {
			byte v = hexCharToByte(hexbyte[0]);
			if(v == -1) {
				return defaultvalue;
			}
			return v;
		}
		return defaultvalue;
	}
	
	public String[] getSubKeys(String parentsection) {
		HashSet<String> keys = new HashSet<>();
		int parentsectionlength = parentsection.length();
		for(String key : kv.keySet()) {
			int keylength = key.length();
			if(keylength < parentsectionlength || !key.startsWith(parentsection)) {
				continue;
			}
			int sectionend = key.indexOf('\0', parentsectionlength);
			if(sectionend == -1) {
				sectionend = keylength;
			}
			keys.add(key.substring(parentsectionlength, sectionend));

		}
		return keys.toArray(new String[keys.size()]);
	}
	
	private static byte hexCharToByte(int c) {
		if((0xFFFFFFD8 & c) == 0x40) {
			c &= 0x07;
			--c;
			if(((0x06 & c) == 0x06)) {
				return -1;
			} else {
				return (byte) (c + 0x0A);
			}
		} else if((0xFFFFFFF8 & c) == 0x30 || (0xFFFFFFFE & c) == 0x38) {
			return (byte) (c & 0x0F);
		} else {
			return -1;
		}
	}

}
