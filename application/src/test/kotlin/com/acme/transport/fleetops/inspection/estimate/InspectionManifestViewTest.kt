package com.acme.transport.fleetops.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionManifest("code-230", "title-286")
        val view = toInspectionManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionManifestViews(mutableListOf<InspectionManifest>(item, item)).size, "views")
    }
}
