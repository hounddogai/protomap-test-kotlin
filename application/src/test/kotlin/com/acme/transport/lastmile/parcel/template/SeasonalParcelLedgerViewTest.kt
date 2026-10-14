package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalParcelLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalParcelLedger("code-840", "name-674")
        val view = toSeasonalParcelLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalParcelLedgerViews(mutableListOf<SeasonalParcelLedger>(item, item)).size, "views")
    }
}
