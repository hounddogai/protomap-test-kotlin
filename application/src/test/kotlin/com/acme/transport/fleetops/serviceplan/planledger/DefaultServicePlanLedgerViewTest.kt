package com.acme.transport.fleetops.serviceplan.planledger

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultServicePlanLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultServicePlanLedger("code-802", "label-248")
        val view = toDefaultServicePlanLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultServicePlanLedgerViews(mutableListOf<DefaultServicePlanLedger>(item, item)).size, "views")
    }
}
