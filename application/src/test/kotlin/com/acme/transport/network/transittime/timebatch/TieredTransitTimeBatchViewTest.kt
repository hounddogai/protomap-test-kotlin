package com.acme.transport.network.transittime.timebatch

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredTransitTimeBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredTransitTimeBatch("code-371", "name-976")
        val view = toTieredTransitTimeBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredTransitTimeBatchViews(mutableListOf<TieredTransitTimeBatch>(item, item)).size, "views")
    }
}
