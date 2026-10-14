package com.acme.transport.network.cutoff.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCutoffLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCutoffLedger("code-919", "title-163")
        val view = toFallbackCutoffLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCutoffLedgerViews(mutableListOf<FallbackCutoffLedger>(item, item)).size, "views")
    }
}
