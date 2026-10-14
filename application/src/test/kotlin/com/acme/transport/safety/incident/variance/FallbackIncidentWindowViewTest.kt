package com.acme.transport.safety.incident.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackIncidentWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackIncidentWindow("code-539", "label-741")
        val view = toFallbackIncidentWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackIncidentWindowViews(mutableListOf<FallbackIncidentWindow>(item, item)).size, "views")
    }
}
