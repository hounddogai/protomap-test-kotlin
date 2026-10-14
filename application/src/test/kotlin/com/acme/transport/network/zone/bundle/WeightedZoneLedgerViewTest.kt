package com.acme.transport.network.zone.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedZoneLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedZoneLedger("code-195", "title-346")
        val view = toWeightedZoneLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedZoneLedgerViews(mutableListOf<WeightedZoneLedger>(item, item)).size, "views")
    }
}
