package ch.so.agi.parcelinfoservice;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.WKBWriter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;

@Controller
public class MainController {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LSNACHFUEHRUNG = "dm01vch24lv95dliegenschaften_lsnachfuehrung";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_GRUNDSTUECK = "dm01vch24lv95dliegenschaften_grundstueck";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LIEGENSCHAFT = "dm01vch24lv95dliegenschaften_liegenschaft";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_SELBSTRECHT = "dm01vch24lv95dliegenschaften_selbstrecht";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_BERGWERK = "dm01vch24lv95dliegenschaften_bergwerk";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_PROJGRUNDSTUECK = "dm01vch24lv95dliegenschaften_projgrundstueck";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_PROJLIEGENSCHAFT = "dm01vch24lv95dliegenschaften_projliegenschaft";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_PROJSELBSTRECHT = "dm01vch24lv95dliegenschaften_projselbstrecht";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_PROJBERGWERK = "dm01vch24lv95dliegenschaften_projbergwerk";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper jacksonObjectMapper;
    
    @Value("${app.dbschema}")
    private String dbschema;
    
    @GetMapping("/")
    public ResponseEntity<String>  ping() {
        return new ResponseEntity<String>("egrid-service", HttpStatus.OK);
    }
    
    // http://localhost:8080/getparcel?XY=2600456,1215400
    @CrossOrigin
    @GetMapping(value="/getparcel", consumes=MediaType.ALL_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getEgridByXY(@RequestParam(value="XY", required=false) String xy, @RequestParam(value="GNSS", required=false) String gnss) {
        if(xy==null && gnss==null) {
            throw new IllegalArgumentException("parameter XY or GNSS required");
        } else if(xy!=null && gnss!=null) {
            throw new IllegalArgumentException("only one of parameters XY or GNSS is allowed");
        }

        Coordinate coord = null;
        int srid = 2056;
        double scale = 1000.0;
        if(xy!=null) {
            coord = parseCoord(xy);
        } else {
            coord = parseCoord(gnss);
            srid = 4326;
            scale = 100000.0;
        }

        WKBWriter geomEncoder = new WKBWriter(2, ByteOrderValues.BIG_ENDIAN, true);
        PrecisionModel precisionModel = new PrecisionModel(scale);
        GeometryFactory geomFact = new GeometryFactory(precisionModel, srid);
        byte geom[] = geomEncoder.write(geomFact.createPoint(coord));
        
        // stateOf: Um zu prüfen, ob der Stand dieses Objektes mit dem vermeintlich gleichen Objekt
        // aus einer anderen Quelle ("Fachservice") übereinstimmt. 
        // Muss sich zeigen, ob das sinnvoll ist.
        String sql = ""
                +" SELECT egris_egrid,nummer,g.nbident,art,CAST('valid' AS text) AS gueltigkeit,TO_CHAR(nf.gueltigereintrag, 'yyyy-mm-dd') AS gueltigereintrag,ST_AsGeoJSON(geometrie) AS geojson FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_GRUNDSTUECK+" g"
                +" LEFT JOIN (SELECT liegenschaft_von as von, geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LIEGENSCHAFT
                +" UNION SELECT selbstrecht_von as von,       geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_SELBSTRECHT
                +" UNION SELECT bergwerk_von as von,          geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_BERGWERK+") b ON b.von=g.t_id"
                +" LEFT JOIN " +getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LSNACHFUEHRUNG+" nf ON g.entstehung = nf.t_id WHERE ST_DWithin(ST_Transform(?,2056),b.geometrie,1.0)"
                +" UNION ALL"
                +" SELECT egris_egrid,nummer,g.nbident,art,CAST('planned' AS text) AS gueltigkeit,TO_CHAR(nf.gueltigereintrag, 'yyyy-mm-dd') AS gueltigereintrag,ST_AsGeoJSON(geometrie) AS geojson FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_PROJGRUNDSTUECK+" g"
                +" LEFT JOIN (SELECT projliegenschaft_von as von, geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_PROJLIEGENSCHAFT
                +" UNION SELECT projselbstrecht_von as von,       geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_PROJSELBSTRECHT
                +" UNION SELECT projbergwerk_von as von,          geometrie FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_PROJBERGWERK+") b ON b.von=g.t_id"
                +" LEFT JOIN " +getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LSNACHFUEHRUNG+" nf ON g.entstehung = nf.t_id WHERE ST_DWithin(ST_Transform(?,2056),b.geometrie,1.0)";

        List<Map<String, Object>> gsList = jdbcTemplate.queryForList(sql, geom, geom);
        
        ObjectNode rootNode = jacksonObjectMapper.createObjectNode();
        rootNode.put("type", "FeatureCollection");
        
        ArrayNode featureArrayNode = jacksonObjectMapper.createArrayNode();
        rootNode.set("features", featureArrayNode);
        
        gsList.stream().forEach(m -> {            
            ObjectNode featureNode = jacksonObjectMapper.createObjectNode();
            featureNode.put("type", "Feature");
            ObjectNode propertiesNode = jacksonObjectMapper.createObjectNode();
            propertiesNode.put("egrid", (String) m.get("egris_egrid"));
            propertiesNode.put("number", (String) m.get("nummer"));
            propertiesNode.put("identDN", (String) m.get("nbident"));
            propertiesNode.put("type", (String) m.get("art"));
            propertiesNode.put("validityType", (String) m.get("gueltigkeit"));
            propertiesNode.put("stateOf", (String) m.get("gueltigereintrag"));
            featureNode.set("properties", propertiesNode);
            featureNode.putRawValue("geometry", new RawValue((String) m.get("geojson")));
            featureArrayNode.add(featureNode);
        });
        
//        logger.info(rootNode.toPrettyString());
        
        return new ResponseEntity<ObjectNode>(rootNode,gsList.size()>0?HttpStatus.OK:HttpStatus.NO_CONTENT);
    }
    
    private String getSchema() {
        return dbschema!=null?dbschema:"xoereb";
    }
    
    private Coordinate parseCoord(String xy) {
        int sepPos = xy.indexOf(',');
        double x = Double.parseDouble(xy.substring(0, sepPos));
        double y = Double.parseDouble(xy.substring(sepPos+1));
        Coordinate coord = new Coordinate(x,y);
        return coord;
    }
}
