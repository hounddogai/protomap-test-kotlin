package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationLedger("code-634", "title-944")
        val view = toUtilizationLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationLedgerViews(mutableListOf<UtilizationLedger>(item, item)).size, "views")
    }
}
