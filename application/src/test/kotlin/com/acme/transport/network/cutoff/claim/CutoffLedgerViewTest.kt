package com.acme.transport.network.cutoff.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffLedger("code-158", "name-697")
        val view = toCutoffLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffLedgerViews(mutableListOf<CutoffLedger>(item, item)).size, "views")
    }
}
