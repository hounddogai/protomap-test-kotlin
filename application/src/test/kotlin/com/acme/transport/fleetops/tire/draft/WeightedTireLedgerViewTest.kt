package com.acme.transport.fleetops.tire.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedTireLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedTireLedger("code-807", "name-330")
        val view = toWeightedTireLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedTireLedgerViews(mutableListOf<WeightedTireLedger>(item, item)).size, "views")
    }
}
