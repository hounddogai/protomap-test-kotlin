package com.acme.transport.network.zone.zonetier

import kotlin.test.Test
import kotlin.test.assertEquals

class ZoneLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newZoneLedger("code-221", "title-113")
        val view = toZoneLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toZoneLedgerViews(mutableListOf<ZoneLedger>(item, item)).size, "views")
    }
}
