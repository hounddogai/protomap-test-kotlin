package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveServicePlanManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveServicePlanManifest("code-627", "title-742")
        val view = toAdaptiveServicePlanManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveServicePlanManifestViews(mutableListOf<AdaptiveServicePlanManifest>(item, item)).size, "views")
    }
}
