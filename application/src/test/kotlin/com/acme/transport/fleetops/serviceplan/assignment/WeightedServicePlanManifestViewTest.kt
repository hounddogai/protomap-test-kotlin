package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedServicePlanManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedServicePlanManifest("code-935", "title-19")
        val view = toWeightedServicePlanManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedServicePlanManifestViews(mutableListOf<WeightedServicePlanManifest>(item, item)).size, "views")
    }
}
