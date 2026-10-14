package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalBackhaulLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalBackhaulLedger("code-665", "name-674")
        val view = toRegionalBackhaulLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalBackhaulLedgerViews(mutableListOf<RegionalBackhaulLedger>(item, item)).size, "views")
    }
}
