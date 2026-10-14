package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterMetric("code-566", "name-753")
        val view = toRosterMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterMetricViews(mutableListOf<RosterMetric>(item, item)).size, "views")
    }
}
