package com.acme.transport.lastmile.drop.target

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveDropLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveDropLedger("code-930", "title-345")
        val view = toAdaptiveDropLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveDropLedgerViews(mutableListOf<AdaptiveDropLedger>(item, item)).size, "views")
    }
}
