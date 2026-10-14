package com.acme.transport.fleetops.serviceplan.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryServicePlanLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryServicePlanLedger("code-634", "label-912")
        val view = toPrimaryServicePlanLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryServicePlanLedgerViews(mutableListOf<PrimaryServicePlanLedger>(item, item)).size, "views")
    }
}
