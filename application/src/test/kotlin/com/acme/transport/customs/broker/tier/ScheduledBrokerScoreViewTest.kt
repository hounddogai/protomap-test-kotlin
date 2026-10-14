package com.acme.transport.customs.broker.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledBrokerScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledBrokerScore("code-541", "label-300")
        val view = toScheduledBrokerScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledBrokerScoreViews(mutableListOf<ScheduledBrokerScore>(item, item)).size, "views")
    }
}
