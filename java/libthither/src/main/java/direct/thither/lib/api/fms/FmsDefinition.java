package direct.thither.lib.api.fms;
/*
 * Author Kashirin Alex (kashirin.alex@gmail.com)
 * THITHER.DIRECT
 * */


public class FmsDefinition{
    public String id, name, desc, operation, section_id, unit_id;
    public Integer timezone, timebase;
    public FmsDefinitionType type;

    public FmsDefinition(FmsDefinitionType typ, String identifier, String n){
        type = typ;
        id = identifier;
        name = n;
    }

    public FmsDefinition(FmsDefinitionType typ, String identifier, String n, String d){
        type = typ;
        id = identifier;
        name = n;
        desc = d;
    }

    // metric
    public FmsDefinition(FmsDefinitionType typ, String identifier, String n, String d,
                         String s, String u, String oper,
                         Integer tz, Integer base){
        type = typ;
        id = identifier;
        name = n;
        desc = d;
        section_id = s;
        unit_id = u;
        operation = oper;
        timezone = tz;
        timebase = base;
    }

    public String toString(){
        String s = getClass().getSimpleName();
        switch (type){
            case UNITS:
                s+=String.format(
                        "(%s=%s, %s=%s, %s=%s, %s=%s)",
                        "Type", type.name(), "id", id, "name", name, "desc", desc);
                break;
            case SECTIONS:
                s+=String.format(
                        "(%s=%s, %s=%s, %s=%s, %s=%s)",
                        "Type", type.name(), "id", id, "name", name, "desc", desc);
                break;
            case METRICS:
                s+=String.format(
                        "(%s=%s, %s=%s, %s=%s, %s=%s, %s=%s, %s=%s, %s=%s, %s=%d, %s=%d)",
                        "Type", type.name(), "id", id, "name", name, "desc", desc,
                        "section", section_id, "unit_id", unit_id, "operation", operation,
                        "timezone", timezone, "timebase", timebase);
                break;
        }
        return s;
    }
}
