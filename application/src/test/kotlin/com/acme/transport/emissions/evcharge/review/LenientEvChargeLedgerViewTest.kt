package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientEvChargeLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientEvChargeLedger("code-275", "label-26")
        val view = toLenientEvChargeLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientEvChargeLedgerViews(mutableListOf<LenientEvChargeLedger>(item, item)).size, "views")
    }
}
