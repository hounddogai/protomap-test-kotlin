package com.acme.transport.network.servicearea.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalServiceAreaLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalServiceAreaLedger("code-850", "title-878")
        val view = toSeasonalServiceAreaLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalServiceAreaLedgerViews(mutableListOf<SeasonalServiceAreaLedger>(item, item)).size, "views")
    }
}
