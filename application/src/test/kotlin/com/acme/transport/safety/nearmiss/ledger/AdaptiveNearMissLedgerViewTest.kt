package com.acme.transport.safety.nearmiss.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveNearMissLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveNearMissLedger("code-503", "label-788")
        val view = toAdaptiveNearMissLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveNearMissLedgerViews(mutableListOf<AdaptiveNearMissLedger>(item, item)).size, "views")
    }
}
