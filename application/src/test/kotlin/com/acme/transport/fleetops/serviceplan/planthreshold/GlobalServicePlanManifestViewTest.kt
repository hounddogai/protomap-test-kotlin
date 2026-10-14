package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalServicePlanManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalServicePlanManifest("code-289", "label-515")
        val view = toGlobalServicePlanManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalServicePlanManifestViews(mutableListOf<GlobalServicePlanManifest>(item, item)).size, "views")
    }
}
