/*
 * Copyright 2016-2017 devemux86
 * Copyright 2017 nebular
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.oscim.android.test;

import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ClusterMarkerRenderer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerLayer;
import org.oscim.layers.marker.MarkerRenderer;
import org.oscim.layers.marker.MarkerRendererFactory;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;

import java.util.ArrayList;
import java.util.List;

import static org.oscim.android.canvas.AndroidGraphics.drawableToBitmap;

public class ClusterMarkerOverlayActivity extends MarkerOverlayActivity {

    private static final int COUNT = 5;
    private static final float STEP = 100f / 110000f; // roughly 100 meters

    @Override
    void createLayers() {
        // Map events receiver
        mMap.layers().add(new MapEventsReceiver(mMap));

        TileSource tileSource = OSciMap4TileSource.builder()
                .httpFactory(new OkHttpEngine.OkHttpFactory())
                .build();
        VectorTileLayer l = mMap.setBaseMap(tileSource);
        mMap.layers().add(new BuildingLayer(mMap, l));
        mMap.layers().add(new LabelLayer(mMap, l));
        mMap.setTheme(VtmThemes.DEFAULT);

        Bitmap bitmapPoi = drawableToBitmap(getResources().getDrawable(R.drawable.marker_poi));
        final MarkerSymbol symbol;
        if (BILLBOARDS)
            symbol = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.BOTTOM_CENTER);
        else
            symbol = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.CENTER, false);

        MarkerRendererFactory markerRendererFactory = new MarkerRendererFactory() {
            @Override
            public MarkerRenderer create(MarkerLayer markerLayer) {
                return new ClusterMarkerRenderer(markerLayer, symbol, new ClusterMarkerRenderer.ClusterStyle(Color.WHITE, Color.BLUE)) {
                    @Override
                    protected Bitmap getClusterBitmap(int size) {
                        // Can customize cluster bitmap here
                        return super.getClusterBitmap(size);
                    }
                };
            }
        };
        mMarkerLayer = new ItemizedLayer<>(
                mMap,
                new ArrayList<MarkerItem>(),
                markerRendererFactory,
                this);
        mMap.layers().add(mMarkerLayer);

        // Create some markers spaced STEP degrees
        List<MarkerItem> pts = new ArrayList<>();
        GeoPoint center = mMap.getMapPosition().getGeoPoint();
        for (int x = -COUNT; x < COUNT; x++) {
            for (int y = -COUNT; y < COUNT; y++) {
                double random = STEP * Math.random() * 2;
                MarkerItem item = new MarkerItem(y + ", " + x, "",
                        new GeoPoint(center.getLatitude() + y * STEP + random, center.getLongitude() + x * STEP + random)
                );
                pts.add(item);
            }
        }
        mMarkerLayer.addItems(pts);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* ignore saved position */
        mMap.setMapPosition(53.08, 8.83, 1 << 15);
    }
}
