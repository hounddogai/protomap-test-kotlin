package com.acme.transport.crewing.restbreak.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRestBreakLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRestBreakLedger("code-283", "title-227")
        val view = toSeasonalRestBreakLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRestBreakLedgerViews(mutableListOf<SeasonalRestBreakLedger>(item, item)).size, "views")
    }
}
