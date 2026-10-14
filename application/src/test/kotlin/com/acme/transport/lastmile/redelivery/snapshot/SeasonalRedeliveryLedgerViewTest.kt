package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRedeliveryLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRedeliveryLedger("code-580", "label-920")
        val view = toSeasonalRedeliveryLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRedeliveryLedgerViews(mutableListOf<SeasonalRedeliveryLedger>(item, item)).size, "views")
    }
}
