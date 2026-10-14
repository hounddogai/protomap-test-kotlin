package com.acme.transport.safety.incident.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredIncidentCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredIncidentCycle("code-766", "name-832")
        val view = toTieredIncidentCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredIncidentCycleViews(mutableListOf<TieredIncidentCycle>(item, item)).size, "views")
    }
}
