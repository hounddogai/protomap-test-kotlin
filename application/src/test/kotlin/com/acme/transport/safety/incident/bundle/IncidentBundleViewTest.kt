package com.acme.transport.safety.incident.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentBundle("code-7", "name-362")
        val view = toIncidentBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentBundleViews(mutableListOf<IncidentBundle>(item, item)).size, "views")
    }
}
