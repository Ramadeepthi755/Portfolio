package com.ramadeepthi.portfolio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController @RequestMapping("/api")
public class AnalyticsController {
  private final JdbcTemplate db; private final ObjectMapper mapper;
  private static final Pattern EMAIL=Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  private static final List<String> TYPES=List.of("page_view","page_time","link_click","contact_submit");
  public AnalyticsController(JdbcTemplate db,ObjectMapper mapper){this.db=db;this.mapper=mapper;}
  @GetMapping("/health") public Map<String,String> health(){ db.queryForObject("SELECT 1", Integer.class); return Map.of("status","ok"); }
  @PostMapping(value="/events", consumes={"application/json", "text/plain", "text/plain;charset=UTF-8"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void event(@RequestBody String rawBody, @RequestHeader(value="Content-Type", required=false) String contentType){
    final Map<String,Object> body;
    try { body=mapper.readValue(rawBody, mapper.getTypeFactory().constructMapType(Map.class,String.class,Object.class)); }
    catch(Exception ex){ throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid event payload"); }
    String type=clip(body.get("event_type"),32);
    if(!TYPES.contains(type)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported event type");
    String path=clip(body.getOrDefault("page_path","/"),500);
    String session=clip(body.get("session_id"),120);
    String visitor=clip(body.get("visitor_id"),120);
    String referrer=clip(body.get("referrer"),1000);
    Object rawMetadata=body.getOrDefault("metadata",Map.of());
    if(!(rawMetadata instanceof Map)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Event metadata must be an object");
    try {
      String metadata=mapper.writeValueAsString(rawMetadata);
      db.update("INSERT INTO portfolio_events(event_type,page_path,metadata,session_id,visitor_id,referrer) VALUES(?,?,CAST(? AS JSON),?,?,?)",type,path.isBlank()?"/":path,metadata,nullable(session),nullable(visitor),nullable(referrer));
    } catch(JsonProcessingException ex){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid event metadata");}
  }

  @GetMapping("/admin/diagnostics")
  public Map<String,Object> diagnostics(){
    Long events=db.queryForObject("SELECT COUNT(*) FROM portfolio_events",Long.class);
    Long contacts=db.queryForObject("SELECT COUNT(*) FROM portfolio_contacts",Long.class);
    List<Map<String,Object>> latest=db.queryForList("SELECT event_type, created_at FROM portfolio_events ORDER BY created_at DESC LIMIT 1");
    return Map.of("database","connected","event_count",events==null?0:events,"contact_count",contacts==null?0:contacts,"latest_event",latest.isEmpty()?Map.of():latest.get(0));
  }
  @PostMapping("/contact") @ResponseStatus(HttpStatus.CREATED)
  public Map<String,String> contact(@RequestBody ContactRequest c,HttpServletRequest request){
    if(c.name()==null||c.name().isBlank()||c.name().trim().length()>100||c.email()==null||c.email().length()>200||!EMAIL.matcher(c.email().trim()).matches()||(c.subject()!=null&&c.subject().length()>160)||c.message()==null||c.message().isBlank()||c.message().trim().length()>4000)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Please check the name, email, subject, and message fields.");
    db.update("INSERT INTO portfolio_contacts(name,email,subject,message) VALUES(?,?,?,?)",c.name().trim(),c.email().trim(),c.subject()==null?"":c.subject().trim(),c.message().trim());
    try {
      String referer=request.getHeader("Referer");
      boolean isDev=referer!=null&&(referer.contains("localhost")||referer.contains("127.0.0.1"));
      String metadata=mapper.writeValueAsString(Map.of("source","contact_form","is_dev",isDev));
      db.update("INSERT INTO portfolio_events(event_type,page_path,metadata,referrer) VALUES('contact_submit','/contact',CAST(? AS JSON),?)",metadata,referer);
    } catch(JsonProcessingException ignored){}
    return Map.of("status","saved");
  }
  @GetMapping("/admin/analytics")
  public List<Map<String,Object>> analytics(
      @RequestParam(defaultValue="7") int days,
      @RequestParam(defaultValue="false") boolean includeDev) {
    int d=Math.max(1,Math.min(days,90));
    RowMapper<Map<String,Object>> row=(rs,n)->Map.of(
      "id",rs.getLong("id"),"event_type",rs.getString("event_type"),
      "page_path",rs.getString("page_path"),"metadata",parse(rs.getString("metadata")),
      "session_id",str(rs.getString("session_id")),"visitor_id",str(rs.getString("visitor_id")),
      "referrer",str(rs.getString("referrer")),"created_at",rs.getTimestamp("created_at").toInstant().toString());
    String sql = "SELECT id,event_type,page_path,CAST(metadata AS CHAR) AS metadata,session_id,visitor_id,referrer,created_at FROM portfolio_events WHERE created_at >= ? "
      + (includeDev ? "" : "AND COALESCE(JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.is_dev')), 'false') <> 'true' ")
      + "ORDER BY created_at DESC LIMIT 10000";
    return db.query(sql,row,Timestamp.from(Instant.now().minus(d,ChronoUnit.DAYS)));
  }
  @GetMapping("/admin/contacts") public List<Map<String,Object>> contacts(@RequestParam(defaultValue="7") int days){int d=Math.max(1,Math.min(days,90));return db.queryForList("SELECT id,name,email,subject,message,created_at FROM portfolio_contacts WHERE created_at >= ? ORDER BY created_at DESC LIMIT 1000",Timestamp.from(Instant.now().minus(d,ChronoUnit.DAYS)));}
  private String clip(Object v,int max){if(v==null)return "";String s=String.valueOf(v).trim();return s.length()>max?s.substring(0,max):s;} private String nullable(String s){return s==null||s.isBlank()?null:s;} private String str(String s){return s==null?"":s;} private Object parse(String s){try{return mapper.readValue(s,Map.class);}catch(Exception e){return Map.of();}}
  public record ContactRequest(String name,String email,String subject,String message){}
}
