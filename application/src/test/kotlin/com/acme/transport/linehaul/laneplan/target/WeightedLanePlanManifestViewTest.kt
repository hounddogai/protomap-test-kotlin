package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedLanePlanManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedLanePlanManifest("code-558", "title-293")
        val view = toWeightedLanePlanManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedLanePlanManifestViews(mutableListOf<WeightedLanePlanManifest>(item, item)).size, "views")
    }
}
