package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalHubLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalHubLedger("code-247", "name-604")
        val view = toSeasonalHubLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalHubLedgerViews(mutableListOf<SeasonalHubLedger>(item, item)).size, "views")
    }
}
