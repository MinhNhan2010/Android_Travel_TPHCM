package com.example.nhom_15_dulichtphcm.ui.components;

import com.example.nhom_15_dulichtphcm.data.model.AdministrativeDistrict;
import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.model.LatLngPoint;
import com.example.nhom_15_dulichtphcm.data.model.PublicUtility;
import com.example.nhom_15_dulichtphcm.data.model.Restaurant;
import com.example.nhom_15_dulichtphcm.data.model.SpecialtyFood;
import com.example.nhom_15_dulichtphcm.data.remote.TravelDataService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

/**
 * Generates interactive Leaflet + MarkerCluster HTML matching the original InteractiveMapView:
 * - Google Maps Tile Server (Google Maps / Traffic / Satellite Hybrid / Dark) with OSM & Esri fallback
 * - Smart MarkerCluster gradient bubbles (2, 9, 33, 5...) + modern teardrop SVG pins
 * - Post-merger administrative boundary polygons + OSRM driving route polylines + live GPS dot.
 */
public final class MapHtmlBuilder {

    private MapHtmlBuilder() {}

    public static String buildHtml(
            List<Attraction> attractions,
            List<SpecialtyFood> foods,
            List<PublicUtility> utilities,
            List<AdministrativeDistrict> districts,
            String filterMode,
            String tileStyle,
            double centerLat,
            double centerLng,
            int zoom
    ) {
        return buildHtml(
                attractions,
                foods,
                utilities,
                districts,
                filterMode,
                tileStyle,
                centerLat,
                centerLng,
                zoom,
                null
        );
    }

    public static String buildHtml(
            List<Attraction> attractions,
            List<SpecialtyFood> foods,
            List<PublicUtility> utilities,
            List<AdministrativeDistrict> districts,
            String filterMode,
            String tileStyle,
            double centerLat,
            double centerLng,
            int zoom,
            String selectedDistrictId
    ) {
        String mode = filterMode != null ? filterMode : "ALL";
        String style = tileStyle != null ? tileStyle : "voyager";

        boolean isDistrictsMode = "DISTRICTS".equals(mode);
        boolean isNewUpdatesMode = "NEW_UPDATES".equals(mode);
        boolean showAttractions = "ALL".equals(mode) || "ATTRACTIONS".equals(mode) || isNewUpdatesMode;
        boolean showFoods = "ALL".equals(mode) || "FOOD".equals(mode) || "RESTAURANTS".equals(mode);
        boolean showUtils = "ALL".equals(mode) || "UTILITIES".equals(mode);

        JSONArray markersJson = new JSONArray();
        JSONArray polygonsJson = new JSONArray();

        try {
            if (showAttractions && attractions != null) {
                for (Attraction a : attractions) {
                    boolean isNew = a.id != null && a.id.startsWith("sync_");
                    if (isNewUpdatesMode && !isNew) {
                        continue;
                    }
                    JSONObject m = new JSONObject();
                    m.put("id", a.id);
                    m.put("kind", "attraction");
                    m.put("glyph", "spot");
                    m.put("lat", a.latitude);
                    m.put("lng", a.longitude);
                    m.put("title", a.nameVi);
                    m.put("sub", a.address != null && !a.address.isEmpty() ? a.address : (a.province + " • " + a.district));
                    m.put("color", isNew ? "#10B981" : "#2563EB");
                    m.put("isNew", isNew);
                    markersJson.put(m);
                }
            }
            if (showFoods && foods != null) {
                for (SpecialtyFood f : foods) {
                    for (Restaurant r : f.restaurants) {
                        JSONObject m = new JSONObject();
                        m.put("id", r.id);
                        m.put("foodId", f.id);
                        m.put("kind", "restaurant");
                        m.put("glyph", "food");
                        m.put("lat", r.latitude);
                        m.put("lng", r.longitude);
                        m.put("title", r.name);
                        m.put("sub", r.address != null && !r.address.isEmpty() ? r.address : (f.nameVi + " • " + r.district));
                        m.put("color", "#EA580C");
                        m.put("isNew", false);
                        markersJson.put(m);
                    }
                }
            }
            if (showUtils && utilities != null) {
                for (PublicUtility u : utilities) {
                    JSONObject m = new JSONObject();
                    m.put("id", u.id);
                    m.put("kind", "utility");
                    m.put("glyph", "util");
                    m.put("lat", u.latitude);
                    m.put("lng", u.longitude);
                    m.put("title", u.nameVi);
                    m.put("sub", u.address != null && !u.address.isEmpty() ? u.address : (u.district + " • " + u.operatingHours));
                    m.put("color", "#0D9488");
                    m.put("isNew", false);
                    markersJson.put(m);
                }
            }
            if (isDistrictsMode && districts != null) {
                for (AdministrativeDistrict d : districts) {
                    JSONObject poly = new JSONObject();
                    poly.put("id", d.id);
                    poly.put("name", d.nameVi);
                    poly.put("type", d.type.displayNameVi);
                    poly.put("zone", d.zone.labelVi);
                    poly.put("color", d.zone.colorHex);
                    poly.put("area", d.areaKm2);
                    poly.put("population", d.population);
                    poly.put("wards", d.wardCount);
                    poly.put("merger", d.mergerStatus);
                    poly.put("highlights", String.join(", ", d.touristHighlights));
                    JSONArray pts = new JSONArray();
                    for (LatLngPoint pt : d.boundaryPolygon) {
                        JSONArray pair = new JSONArray();
                        pair.put(pt.latitude);
                        pair.put(pt.longitude);
                        pts.put(pair);
                    }
                    poly.put("points", pts);
                    polygonsJson.put(poly);
                }
            }
        } catch (Exception ignored) {
        }

        String roadmapTpl = TravelDataService.buildGoogleTileTemplate("roadmap");
        String satTpl = TravelDataService.buildGoogleTileTemplate("hybrid");
        String selDistSafe = selectedDistrictId != null ? selectedDistrictId.replace("'", "") : "";
        boolean fitAll = isNewUpdatesMode;

        StringBuilder html = new StringBuilder(12_000);
        html.append("<!DOCTYPE html><html><head><meta charset='utf-8'/>")
            .append("<meta name='viewport' content='width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no'/>")
            .append("<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>")
            .append("<link rel='stylesheet' href='https://unpkg.com/leaflet.markercluster@1.5.3/dist/MarkerCluster.css'/>")
            .append("<link rel='stylesheet' href='https://unpkg.com/leaflet.markercluster@1.5.3/dist/MarkerCluster.Default.css'/>")
            .append("<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>")
            .append("<script src='https://unpkg.com/leaflet.markercluster@1.5.3/dist/leaflet.markercluster.js'></script>")
            .append("<style>")
            .append("html,body,#map{height:100%;width:100%;margin:0;padding:0;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#F1F5F9;}")
            .append("body.dark-map{background:#0F172A;}")
            .append("body.dark-map .leaflet-tile-pane{filter:invert(92%) hue-rotate(180deg) brightness(95%) contrast(92%);}")
            .append(".leaflet-popup-content-wrapper{border-radius:16px;box-shadow:0 10px 28px rgba(15,23,42,0.22);padding:5px;border:1px solid rgba(226,232,240,0.9);}")
            .append(".popup-title{font-weight:700;font-size:13.5px;color:#0F172A;margin-bottom:3px;}")
            .append(".popup-badge-new{display:inline-block;background:#DCFCE7;color:#047857;font-size:10px;font-weight:800;padding:2px 7px;border-radius:999px;margin-bottom:4px;}")
            .append(".popup-sub{font-size:11.5px;color:#475569;line-height:1.35;margin-bottom:5px;}")
            .append(".popup-coord{font-size:10.5px;font-family:monospace;color:#0369A1;background:#E0F2FE;display:inline-block;padding:2px 7px;border-radius:7px;}")
            .append(".modern-pin{width:30px;height:30px;border-radius:50% 50% 50% 4px;transform:rotate(-45deg);border:2.5px solid #FFFFFF;box-shadow:0 4px 10px rgba(15,23,42,0.35);display:flex;align-items:center;justify-content:center;box-sizing:border-box;}")
            .append(".modern-pin>.pin-inner{transform:rotate(45deg);display:flex;align-items:center;justify-content:center;color:#FFFFFF;font-weight:800;font-size:12px;line-height:1;}")
            .append(".modern-pin.is-new{box-shadow:0 0 0 3px rgba(16,185,129,0.35),0 4px 12px rgba(5,150,105,0.45);}")
            .append(".user-gps-dot{width:18px;height:18px;border-radius:50%;background:#2563EB;border:3px solid #FFFFFF;box-shadow:0 0 0 8px rgba(37,99,235,0.25);}")
            .append(".cluster-bubble{background:linear-gradient(135deg,#2563EB,#0D9488);color:#FFFFFF;border:2.5px solid #FFFFFF;border-radius:50%;box-shadow:0 4px 12px rgba(15,23,42,0.28);font-weight:800;font-size:13px;display:flex;align-items:center;justify-content:center;width:36px;height:36px;}")
            .append("</style></head><body><div id='map'></div><script>")
            .append(String.format(Locale.US, "var map = L.map('map',{zoomControl:true,attributionControl:false}).setView([%.5f,%.5f],%d);", centerLat, centerLng, zoom))
            .append("var baseLayer = null; var fallbackLayer = null;")
            .append("function setTileStyleJs(styleKey){")
            .append("  if(baseLayer){ map.removeLayer(baseLayer); baseLayer=null; }")
            .append("  if(fallbackLayer){ map.removeLayer(fallbackLayer); fallbackLayer=null; }")
            .append("  if(styleKey==='dark'){ document.body.classList.add('dark-map'); } else { document.body.classList.remove('dark-map'); }")
            .append("  var url = '").append(roadmapTpl).append("';")
            .append("  var isSat = false;")
            .append("  if(styleKey==='satellite' || styleKey==='hybrid'){ url = '").append(satTpl).append("'; isSat = true; }")
            .append("  else if(styleKey==='traffic'){ url = 'https://mt{s}.google.com/vt/lyrs=m,traffic&hl=vi&x={x}&y={y}&z={z}'; }")
            .append("  baseLayer = L.tileLayer(url,{subdomains:'0123',maxZoom:20}).addTo(map);")
            .append("  baseLayer.on('tileerror', function(){")
            .append("    if(!fallbackLayer){")
            .append("      var fbUrl = isSat ? 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}' : 'https://tile.openstreetmap.org/{z}/{x}/{y}.png';")
            .append("      fallbackLayer = L.tileLayer(fbUrl,{maxZoom:19}).addTo(map);")
            .append("    }")
            .append("  });")
            .append("}")
            .append("setTileStyleJs('").append(style).append("');")
            .append("var routeShadow = null; var routeLine = null; var userMarker = null;")
            .append("var selectedDistId = '").append(selDistSafe).append("';")
            .append("var targetPolygon = null;")
            .append("var polys = ").append(polygonsJson.toString()).append(";")
            .append("polys.forEach(function(d){")
            .append("  if(d.points && d.points.length > 2){")
            .append("    var isSel = (selectedDistId !== '' && d.id === selectedDistId);")
            .append("    var pg = L.polygon(d.points,{color:d.color,weight:isSel?3:1.75,dashArray:isSel?null:'6 6',opacity:0.85,fillColor:d.color,fillOpacity:isSel?0.24:0.08}).addTo(map);")
            .append("    var pHtml = '<div class=\"popup-title\">🏛️ '+d.name+' ('+d.type+')</div>'")
            .append("      + '<div style=\"font-size:11px;color:#B45309;font-weight:bold;margin-bottom:4px;\">'+d.zone+'</div>'")
            .append("      + '<div style=\"font-size:11px;background:#FEF3C7;padding:5px 8px;border-radius:6px;margin-bottom:6px;color:#92400E;\"><b>Sáp nhập:</b> '+d.merger+'</div>'")
            .append("      + '<div class=\"popup-sub\">📐 '+d.area+' km² • 👥 '+d.population+' ('+d.wards+' phường/xã)</div>';")
            .append("    pg.bindPopup(pHtml);")
            .append("    pg.on('click',function(){ if(window.AndroidBridge){ AndroidBridge.onDistrictClicked(d.id); } });")
            .append("    if(isSel){ targetPolygon = pg; }")
            .append("  }")
            .append("});")
            .append("function buildPinGlyph(g){")
            .append("  if(g==='food') return '<svg width=\"13\" height=\"13\" viewBox=\"0 0 24 24\" fill=\"white\"><path d=\"M11 9H9V2H7v7H5V2H3v7c0 2.12 1.66 3.84 3.75 3.97V22h2.5v-9.03C11.34 12.84 13 11.12 13 9V2h-2v7zm5-3v8h2.5v8H21V2c-2.76 0-5 2.24-5 4z\"/></svg>';")
            .append("  if(g==='util') return '<svg width=\"13\" height=\"13\" viewBox=\"0 0 24 24\" fill=\"white\"><path d=\"M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-2 10h-4v4h-2v-4H7v-2h4V7h2v4h4v2z\"/></svg>';")
            .append("  return '<svg width=\"13\" height=\"13\" viewBox=\"0 0 24 24\" fill=\"white\"><path d=\"M12 2l2.6 5.8L21 8.6l-4.6 4.4 1.1 6.4L12 16.4l-5.5 3 1.1-6.4L3 8.6l6.4-.8z\"/></svg>';")
            .append("}")
            .append("var useCluster = (typeof L.markerClusterGroup === 'function');")
            .append("var markerGroup = useCluster ? L.markerClusterGroup({maxClusterRadius:42,spiderfyOnMaxZoom:true,showCoverageOnHover:false,iconCreateFunction:function(c){return L.divIcon({html:'<div class=\"cluster-bubble\">'+c.getChildCount()+'</div>',className:'custom-cluster',iconSize:L.point(36,36)});}}) : L.featureGroup();")
            .append("var markerByKey = {};")
            .append("var markers = ").append(markersJson.toString()).append(";")
            .append("markers.forEach(function(m){")
            .append("  var nc = m.isNew ? ' is-new' : '';")
            .append("  var pHtml = '<div style=\"background-color:'+m.color+';\" class=\"modern-pin'+nc+'\"><div class=\"pin-inner\">'+buildPinGlyph(m.glyph)+'</div></div>';")
            .append("  var icon = L.divIcon({className:'custom-div-icon',html:pHtml,iconSize:[30,30],iconAnchor:[15,30],popupAnchor:[0,-28]});")
            .append("  var mk = L.marker([m.lat,m.lng],{icon:icon});")
            .append("  var nb = m.isNew ? '<div class=\"popup-badge-new\">✨ MỚI CẬP NHẬT</div>' : '';")
            .append("  mk.bindPopup(nb+'<div class=\"popup-title\">'+m.title+'</div><div class=\"popup-sub\">'+m.sub+'</div><div class=\"popup-coord\">📍 '+m.lat.toFixed(5)+', '+m.lng.toFixed(5)+'</div>');")
            .append("  mk.on('click',function(){ if(window.AndroidBridge){ AndroidBridge.onMarkerClicked(m.kind, m.id); } });")
            .append("  markerGroup.addLayer(mk);")
            .append("  markerByKey[m.kind+'_'+m.id] = mk;")
            .append("});")
            .append("map.addLayer(markerGroup);")
            .append("if(targetPolygon){ map.fitBounds(targetPolygon.getBounds().pad(0.12)); targetPolygon.openPopup(); }")
            .append("else if(").append(fitAll ? "true" : "false").append(" && markers.length > 1){ map.fitBounds(markerGroup.getBounds().pad(0.12)); }")
            .append("function flyToPoint(lat,lng,z){ map.flyTo([lat,lng], z||16, {duration:0.75}); }")
            .append("function focusPlaceKey(key,lat,lng){")
            .append("  var mk = markerByKey[key];")
            .append("  if(mk && useCluster && typeof markerGroup.zoomToShowLayer === 'function'){")
            .append("    map.flyTo([lat,lng], 16, {duration:0.7});")
            .append("    setTimeout(function(){ try{ mk.openPopup(); }catch(e){} }, 750);")
            .append("  } else {")
            .append("    map.flyTo([lat,lng], 16, {duration:0.7});")
            .append("    if(mk){ mk.openPopup(); }")
            .append("  }")
            .append("}")
            .append("function clearRoute(){ if(routeShadow){map.removeLayer(routeShadow);routeShadow=null;} if(routeLine){map.removeLayer(routeLine);routeLine=null;} }")
            .append("function drawRoute(coordsJson){")
            .append("  try{")
            .append("    clearRoute();")
            .append("    var pts = JSON.parse(coordsJson);")
            .append("    if(pts && pts.length > 1){")
            .append("      routeShadow = L.polyline(pts,{color:'#1E3A8A',weight:7.5,opacity:0.35}).addTo(map);")
            .append("      routeLine = L.polyline(pts,{color:'#2563EB',weight:4.5,opacity:0.95}).addTo(map);")
            .append("      map.fitBounds(routeLine.getBounds().pad(0.18));")
            .append("    }")
            .append("  }catch(e){}")
            .append("}")
            .append("function showUserGps(lat,lng){")
            .append("  if(userMarker){ map.removeLayer(userMarker); }")
            .append("  var uIcon = L.divIcon({className:'user-gps-icon',html:'<div class=\"user-gps-dot\"></div>',iconSize:[18,18],iconAnchor:[9,9]});")
            .append("  userMarker = L.marker([lat,lng],{icon:uIcon}).addTo(map).bindPopup('<div class=\"popup-title\">📍 Vị trí hiện tại của bạn</div>');")
            .append("  map.flyTo([lat,lng],15,{duration:0.8});")
            .append("}")
            .append("</script></body></html>");

        return html.toString();
    }
}
