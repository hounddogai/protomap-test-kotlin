package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockQuota("code-313", "name-878")
        val view = toCrossDockQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockQuotaViews(mutableListOf<CrossDockQuota>(item, item)).size, "views")
    }
}
