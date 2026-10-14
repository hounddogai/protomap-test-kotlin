package com.acme.transport.network.transittime.scheduledtimebatch

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTransitTimeBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledTransitTimeBatch("code-754", "name-424")
        val view = toScheduledTransitTimeBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledTransitTimeBatchViews(mutableListOf<ScheduledTransitTimeBatch>(item, item)).size, "views")
    }
}
