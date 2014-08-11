package io.github.jisaacs1207.CompanyCraft;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public abstract class ConfigObject
{
  protected void onLoad(ConfigurationSection cs)
    throws Exception
  {
    for (Field field : getClass().getDeclaredFields())
    {
      String path = field.getName().replaceAll("_", ".");
      path = "Saves." + path;
      if (!doSkip(field)) {
        if (cs.isSet(path)) {
          field.set(this, loadObject(field, cs, path));
        } else {
          cs.set(path, saveObject(field.get(this), field, cs, path));
        }
      }
    }
  }
  
  protected void onSave(ConfigurationSection cs)
    throws Exception
  {
    for (Field field : getClass().getDeclaredFields())
    {
      String path = field.getName().replaceAll("_", ".");
      path = "Saves." + path;
      if (!doSkip(field)) {
        cs.set(path, saveObject(field.get(this), field, cs, path));
      }
    }
  }
  
  protected Object loadObject(Field field, ConfigurationSection cs, String path)
    throws Exception
  {
    return loadObject(field, cs, path, 0);
  }
  
  protected Object saveObject(Object obj, Field field, ConfigurationSection cs, String path)
    throws Exception
  {
    return saveObject(obj, field, cs, path, 0);
  }
  
  protected Object loadObject(Field field, ConfigurationSection cs, String path, int depth)
    throws Exception
  {
    Class<?> clazz = getClassAtDepth(field.getGenericType(), depth);
    if ((ConfigObject.class.isAssignableFrom(clazz)) && (isConfigurationSection(cs.get(path)))) {
      return getConfigObject(clazz, cs.getConfigurationSection(path));
    }
    if ((Location.class.isAssignableFrom(clazz)) && (isJSON(cs.get(path)))) {
      return getLocation((String)cs.get(path));
    }
    if ((Vector.class.isAssignableFrom(clazz)) && (isJSON(cs.get(path)))) {
      return getVector((String)cs.get(path));
    }
    if ((Map.class.isAssignableFrom(clazz)) && (isConfigurationSection(cs.get(path)))) {
      return getMap(field, cs.getConfigurationSection(path), path, depth);
    }
    if ((clazz.isEnum()) && (isString(cs.get(path)))) {
      return getEnum(clazz, (String)cs.get(path));
    }
    if ((List.class.isAssignableFrom(clazz)) && (isConfigurationSection(cs.get(path))))
    {
      Class<?> subClazz = getClassAtDepth(field.getGenericType(), depth + 1);
      if ((ConfigObject.class.isAssignableFrom(subClazz)) || (Location.class.isAssignableFrom(subClazz)) || (Vector.class.isAssignableFrom(subClazz)) || (Map.class.isAssignableFrom(subClazz)) || (List.class.isAssignableFrom(subClazz)) || (subClazz.isEnum())) {
        return getList(field, cs.getConfigurationSection(path), path, depth);
      }
      return cs.get(path);
    }
    return cs.get(path);
  }
  
  protected Object saveObject(Object obj, Field field, ConfigurationSection cs, String path, int depth)
    throws Exception
  {
    Class<?> clazz = getClassAtDepth(field.getGenericType(), depth);
    if ((ConfigObject.class.isAssignableFrom(clazz)) && (isConfigObject(obj))) {
      return getConfigObject((ConfigObject)obj, path, cs);
    }
    if ((Location.class.isAssignableFrom(clazz)) && (isLocation(obj))) {
      return getLocation((Location)obj);
    }
    if ((Vector.class.isAssignableFrom(clazz)) && (isVector(obj))) {
      return getVector((Vector)obj);
    }
    if ((Map.class.isAssignableFrom(clazz)) && (isMap(obj))) {
      return getMap((Map<String, ?>)obj, field, cs, path, depth);
    }
    if ((clazz.isEnum()) && (isEnum(clazz, obj))) {
      return getEnum((Enum<?>)obj);
    }
    if ((List.class.isAssignableFrom(clazz)) && (isList(obj)))
    {
      Class<?> subClazz = getClassAtDepth(field.getGenericType(), depth + 1);
      if ((ConfigObject.class.isAssignableFrom(subClazz)) || (Location.class.isAssignableFrom(subClazz)) || (Vector.class.isAssignableFrom(subClazz)) || (Map.class.isAssignableFrom(subClazz)) || (List.class.isAssignableFrom(subClazz)) || (subClazz.isEnum())) {
        return getList((List<?>)obj, field, cs, path, depth);
      }
      return obj;
    }
    return obj;
  }
  
  protected Class<?> getClassAtDepth(Type type, int depth)
    throws Exception
  {
    if (depth <= 0)
    {
      String className = type.toString();
      if ((className.length() >= 6) && (className.substring(0, 6).equalsIgnoreCase("class "))) {
        className = className.substring(6);
      }
      if (className.indexOf("<") >= 0) {
        className = className.substring(0, className.indexOf("<"));
      }
      try
      {
        return Class.forName(className);
      }
      catch (ClassNotFoundException ex)
      {
        if (className.equalsIgnoreCase("byte")) {
          return Byte.class;
        }
        if (className.equalsIgnoreCase("short")) {
          return Short.class;
        }
        if (className.equalsIgnoreCase("int")) {
          return Integer.class;
        }
        if (className.equalsIgnoreCase("long")) {
          return Long.class;
        }
        if (className.equalsIgnoreCase("float")) {
          return Float.class;
        }
        if (className.equalsIgnoreCase("double")) {
          return Double.class;
        }
        if (className.equalsIgnoreCase("char")) {
          return Character.class;
        }
        if (className.equalsIgnoreCase("boolean")) {
          return Boolean.class;
        }
        throw ex;
      }
    }
    depth--;
    ParameterizedType pType = (ParameterizedType)type;
    Type[] typeArgs = pType.getActualTypeArguments();
    return getClassAtDepth(typeArgs[(typeArgs.length - 1)], depth);
  }
  
  protected boolean isString(Object obj)
  {
    if ((obj instanceof String)) {
      return true;
    }
    return false;
  }
  
  protected boolean isConfigurationSection(Object o)
  {
    try
    {
      return (ConfigurationSection)o != null;
    }
    catch (Exception e) {}
    return false;
  }
  
  protected boolean isJSON(Object obj)
  {
    try
    {
      if ((obj instanceof String))
      {
        String str = (String)obj;
        if (str.startsWith("{")) {
          return new JSONParser().parse(str) != null;
        }
      }
      return false;
    }
    catch (Exception e) {}
    return false;
  }
  
  protected boolean isConfigObject(Object obj)
  {
    try
    {
      return (ConfigObject)obj != null;
    }
    catch (Exception e) {}
    return false;
  }
  
  protected boolean isLocation(Object obj)
  {
    try
    {
      return (Location)obj != null;
    }
    catch (Exception e) {}
    return false;
  }
  
  protected boolean isVector(Object obj)
  {
    try
    {
      return (Vector)obj != null;
    }
    catch (Exception e) {}
    return false;
  }
  
  protected boolean isMap(Object obj)
  {
    try
    {
      return (Map<?, ?>)obj != null;
    }
    catch (Exception e) {}
    return false;
  }
  
  protected boolean isList(Object obj)
  {
    try
    {
      return (List<?>)obj != null;
    }
    catch (Exception e) {}
    return false;
  }
  
  protected boolean isEnum(Class<?> clazz, Object obj)
  {
    if (!clazz.isEnum()) {
      return false;
    }
    for (Object constant : clazz.getEnumConstants()) {
      if (constant.equals(obj)) {
        return true;
      }
    }
    return false;
  }
  
  protected ConfigObject getConfigObject(Class<?> clazz, ConfigurationSection cs)
    throws Exception
  {
    ConfigObject obj = (ConfigObject)clazz.newInstance();
    obj.onLoad(cs);
    return obj;
  }
  
  protected Location getLocation(String json)
    throws Exception
  {
    JSONObject data = (JSONObject)new JSONParser().parse(json);
    
    World world = Bukkit.getWorld((String)data.get("world"));
    
    double x = Double.parseDouble((String)data.get("x"));
    double y = Double.parseDouble((String)data.get("y"));
    double z = Double.parseDouble((String)data.get("z"));
    
    float pitch = Float.parseFloat((String)data.get("pitch"));
    float yaw = Float.parseFloat((String)data.get("yaw"));
    
    Location loc = new Location(world, x, y, z);
    loc.setPitch(pitch);
    loc.setYaw(yaw);
    return loc;
  }
  
  protected Vector getVector(String json)
    throws Exception
  {
    JSONObject data = (JSONObject)new JSONParser().parse(json);
    
    double x = Double.parseDouble((String)data.get("x"));
    double y = Double.parseDouble((String)data.get("y"));
    double z = Double.parseDouble((String)data.get("z"));
    
    return new Vector(x, y, z);
  }
  
  protected Map<String, Object> getMap(Field field, ConfigurationSection cs, String path, int depth)
    throws Exception
  {
    depth++;
    Set<String> keys = cs.getKeys(false);
    Map<String, Object> map = new HashMap<String, Object>();
    if ((keys != null) && (keys.size() > 0)) {
      for (String key : keys)
      {
        Object in = cs.get(key);
        in = loadObject(field, cs, key, depth);
        map.put(key, in);
      }
    }
    return map;
  }
  
  protected List<Object> getList(Field field, ConfigurationSection cs, String path, int depth)
    throws Exception
  {
    depth++;
    int listSize = cs.getKeys(false).size();
    String key = path;
    if (key.lastIndexOf(".") >= 0) {
      key = key.substring(key.lastIndexOf("."));
    }
    List<Object> list = new ArrayList<Object>();
    if (listSize > 0)
    {
      int loaded = 0;
      int i = 0;
      while (loaded < listSize)
      {
        if (cs.isSet(key + i))
        {
          Object in = cs.get(key + i);
          in = loadObject(field, cs, key + i, depth);
          list.add(in);
          loaded++;
        }
        i++;
        if (i > listSize * 3) {
          loaded = listSize;
        }
      }
    }
    return list;
  }
  
  protected Enum<?> getEnum(Class<?> clazz, String string)
    throws Exception
  {
    if (!clazz.isEnum()) {
      throw new Exception("Class " + clazz.getName() + " is not an enum.");
    }
    for (Object constant : clazz.getEnumConstants()) {
      if (((Enum<?>)constant).toString().equals(string)) {
        return (Enum)constant;
      }
    }
    throw new Exception("String " + string + " not a valid enum constant for " + clazz.getName());
  }
  
  protected ConfigurationSection getConfigObject(ConfigObject obj, String path, ConfigurationSection cs)
    throws Exception
  {
    ConfigurationSection subCS = cs.createSection(path);
    obj.onSave(subCS);
    return subCS;
  }
  
  protected String getLocation(Location loc)
  {
    String ret = "{";
    ret = ret + "\"world\":\"" + loc.getWorld().getName() + "\"";
    ret = ret + ",\"x\":\"" + loc.getX() + "\"";
    ret = ret + ",\"y\":\"" + loc.getY() + "\"";
    ret = ret + ",\"z\":\"" + loc.getZ() + "\"";
    ret = ret + ",\"pitch\":\"" + loc.getPitch() + "\"";
    ret = ret + ",\"yaw\":\"" + loc.getYaw() + "\"";
    ret = ret + "}";
    if (!isJSON(ret)) {
      return getLocationJSON(loc);
    }
    try
    {
      getLocation(ret);
    }
    catch (Exception ex)
    {
      return getLocationJSON(loc);
    }
    return ret;
  }
  
  protected String getLocationJSON(Location loc)
  {
    JSONObject data = new JSONObject();
    
    data.put("world", loc.getWorld().getName());
    
    data.put("x", String.valueOf(loc.getX()));
    data.put("y", String.valueOf(loc.getY()));
    data.put("z", String.valueOf(loc.getZ()));
    
    data.put("pitch", String.valueOf(loc.getPitch()));
    data.put("yaw", String.valueOf(loc.getYaw()));
    return data.toJSONString();
  }
  
  protected String getVector(Vector vec)
  {
    String ret = "{";
    ret = ret + "\"x\":\"" + vec.getX() + "\"";
    ret = ret + ",\"y\":\"" + vec.getY() + "\"";
    ret = ret + ",\"z\":\"" + vec.getZ() + "\"";
    ret = ret + "}";
    if (!isJSON(ret)) {
      return getVectorJSON(vec);
    }
    try
    {
      getVector(ret);
    }
    catch (Exception ex)
    {
      return getVectorJSON(vec);
    }
    return ret;
  }
  
  protected String getVectorJSON(Vector vec)
  {
    JSONObject data = new JSONObject();
    
    data.put("x", String.valueOf(vec.getX()));
    data.put("y", String.valueOf(vec.getY()));
    data.put("z", String.valueOf(vec.getZ()));
    return data.toJSONString();
  }
  
  protected ConfigurationSection getMap(Map<String, ?> map, Field field, ConfigurationSection cs, String path, int depth)
    throws Exception
  {
    depth++;
    ConfigurationSection subCS = cs.createSection(path);
    Set<String> keys = map.keySet();
    if ((keys != null) && (keys.size() > 0)) {
      for (String key : keys)
      {
        Object out = map.get(key);
        out = saveObject(out, field, cs, path + "." + key, depth);
        subCS.set(key, out);
      }
    }
    return subCS;
  }
  
  protected ConfigurationSection getList(List<?> list, Field field, ConfigurationSection cs, String path, int depth)
    throws Exception
  {
    depth++;
    ConfigurationSection subCS = cs.createSection(path);
    String key = path;
    if (key.lastIndexOf(".") >= 0) {
      key = key.substring(key.lastIndexOf("."));
    }
    if ((list != null) && (list.size() > 0)) {
      for (int i = 0; i < list.size(); i++)
      {
        Object out = list.get(i);
        out = saveObject(out, field, cs, path + "." + key + (i + 1), depth);
        subCS.set(key + (i + 1), out);
      }
    }
    return subCS;
  }
  
  protected String getEnum(Enum<?> enumObj)
  {
    return enumObj.toString();
  }
  
  protected boolean doSkip(Field field)
  {
    return (Modifier.isTransient(field.getModifiers())) || (Modifier.isStatic(field.getModifiers())) || (Modifier.isFinal(field.getModifiers())) || (Modifier.isPrivate(field.getModifiers()));
  }
}

