package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeBatch("code-436", "name-705")
        val view = toTransitTimeBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeBatchViews(mutableListOf<TransitTimeBatch>(item, item)).size, "views")
    }
}
