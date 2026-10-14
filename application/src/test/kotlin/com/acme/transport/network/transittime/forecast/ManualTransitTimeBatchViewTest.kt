package com.acme.transport.network.transittime.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTransitTimeBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTransitTimeBatch("code-375", "title-527")
        val view = toManualTransitTimeBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTransitTimeBatchViews(mutableListOf<ManualTransitTimeBatch>(item, item)).size, "views")
    }
}
