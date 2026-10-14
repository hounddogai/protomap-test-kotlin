package com.acme.transport.safety.incident.defaultincidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyIncidentManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyIncidentManifest("code-980", "title-202")
        val view = toLegacyIncidentManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyIncidentManifestViews(mutableListOf<LegacyIncidentManifest>(item, item)).size, "views")
    }
}
