package com.acme.transport.fleetops.serviceplan.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackServicePlanLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackServicePlanLedger("code-941", "title-50")
        val view = toFallbackServicePlanLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackServicePlanLedgerViews(mutableListOf<FallbackServicePlanLedger>(item, item)).size, "views")
    }
}
