package com.acme.transport.network.zone.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneBatch("code-106", "title-692")
        val view = toZoneBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneBatchViews(mutableListOf<ZoneBatch>(item, item)).size, "views")
    }
}
