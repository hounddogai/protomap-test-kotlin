package com.acme.transport.safety.incident.incidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingIncidentManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingIncidentManifest("code-779", "title-492")
        val view = toRollingIncidentManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingIncidentManifestViews(mutableListOf<RollingIncidentManifest>(item, item)).size, "views")
    }
}
