package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentTarget("code-53", "name-763")
        val view = toIncidentTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentTargetViews(mutableListOf<IncidentTarget>(item, item)).size, "views")
    }
}
