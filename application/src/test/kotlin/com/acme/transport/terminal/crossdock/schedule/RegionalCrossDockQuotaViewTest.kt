package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCrossDockQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCrossDockQuota("code-773", "label-382")
        val view = toRegionalCrossDockQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCrossDockQuotaViews(mutableListOf<RegionalCrossDockQuota>(item, item)).size, "views")
    }
}
