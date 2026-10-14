package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredFuelBurnLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredFuelBurnLedger("code-154", "label-699")
        val view = toTieredFuelBurnLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredFuelBurnLedgerViews(mutableListOf<TieredFuelBurnLedger>(item, item)).size, "views")
    }
}
