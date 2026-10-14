package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalZoneLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalZoneLedger("code-717", "title-431")
        val view = toRegionalZoneLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalZoneLedgerViews(mutableListOf<RegionalZoneLedger>(item, item)).size, "views")
    }
}
