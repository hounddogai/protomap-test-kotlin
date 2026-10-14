package com.acme.transport.safety.incident.target

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedIncidentTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedIncidentTarget("code-349", "title-485")
        val view = toCappedIncidentTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedIncidentTargetViews(mutableListOf<CappedIncidentTarget>(item, item)).size, "views")
    }
}
