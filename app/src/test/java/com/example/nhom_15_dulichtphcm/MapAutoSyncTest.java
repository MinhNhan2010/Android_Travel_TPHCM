package com.example.nhom_15_dulichtphcm;

import com.example.nhom_15_dulichtphcm.data.model.Attraction;
import com.example.nhom_15_dulichtphcm.data.remote.TravelDataService;
import com.example.nhom_15_dulichtphcm.data.repository.AttractionRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MapAutoSyncTest {

    @Before
    public void setUp() {
        AttractionRepository.resetForTesting();
    }

    @Test
    public void googleTileTemplates_matchExpectedLayers() {
        assertTrue(TravelDataService.buildGoogleTileTemplate("roadmap").contains("lyrs=m"));
        assertTrue(TravelDataService.buildGoogleTileTemplate("satellite").contains("lyrs=s"));
        assertTrue(TravelDataService.buildGoogleTileTemplate("hybrid").contains("lyrs=y"));
        assertTrue(TravelDataService.buildGoogleTileTemplate("terrain").contains("lyrs=p"));
    }

    @Test
    public void wikipediaAndPhotonAndOsrmParsers_extractValidGeospatialEntities() {
        String wikiJson = "{\"query\":{\"geosearch\":[{\"pageid\":9911,\"title\":\"Bảo tàng Áo dài Thủ Đức\",\"lat\":10.8123,\"lon\":106.8345}]}}";
        List<Attraction> wikiParsed = TravelDataService.parseWikipediaGeoJson(wikiJson, "TP.HCM", "TP. Thủ Đức");
        assertEquals(1, wikiParsed.size());
        assertEquals("sync_wiki_9911", wikiParsed.get(0).id);

        String photonJson = "{\"features\":[{\"geometry\":{\"coordinates\":[106.7125,10.7755]},\"properties\":{\"osm_id\":5522,\"name\":\"Saigon Marina Skywalk\",\"district\":\"Phường Thủ Thiêm\",\"osm_value\":\"viewpoint\"}}]}";
        List<Attraction> photonParsed = TravelDataService.parsePhotonGeoJson(photonJson, "TP.HCM", "Phường Thủ Thiêm");
        assertEquals(1, photonParsed.size());
        assertEquals("sync_osm_5522", photonParsed.get(0).id);

        String osrmJson = "{\"routes\":[{\"distance\":4250.0,\"duration\":780.0,\"geometry\":{\"coordinates\":[[106.6980,10.7725],[106.7050,10.7790],[106.7218,10.7950]]}}]}";
        TravelDataService.OsrmRouteResult route = TravelDataService.parseOsrmRouteJson(osrmJson);
        assertNotNull(route);
        assertEquals(4.3, route.distanceKm, 0.15);
        assertEquals(3, route.geometry.size());
    }

    @Test
    public void mergeAutoSyncedPlaces_deduplicatesAndCountsNewEntries() {
        List<Attraction> curated = TravelDataService.curatedLatestPlaces("all");
        assertFalseEmpty(curated);

        AttractionRepository.SyncMergeSummary first =
                AttractionRepository.mergeAutoSyncedPlaces(curated, 1_700_000_000_000L);
        assertTrue(first.addedCount > 0);

        int countAfterFirst = AttractionRepository.getAttractions().size();
        AttractionRepository.SyncMergeSummary second =
                AttractionRepository.mergeAutoSyncedPlaces(curated, 1_700_000_100_000L);
        assertEquals(0, second.addedCount);
        assertEquals(countAfterFirst, AttractionRepository.getAttractions().size());
    }

    private void assertFalseEmpty(List<?> list) {
        assertNotNull(list);
        assertTrue(!list.isEmpty());
    }
}
