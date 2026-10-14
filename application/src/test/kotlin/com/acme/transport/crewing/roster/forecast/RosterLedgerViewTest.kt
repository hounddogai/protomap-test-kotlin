package com.acme.transport.crewing.roster.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterLedger("code-235", "label-159")
        val view = toRosterLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterLedgerViews(mutableListOf<RosterLedger>(item, item)).size, "views")
    }
}
