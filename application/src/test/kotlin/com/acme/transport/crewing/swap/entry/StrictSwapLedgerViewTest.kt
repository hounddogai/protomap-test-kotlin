package com.acme.transport.crewing.swap.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictSwapLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictSwapLedger("code-917", "name-892")
        val view = toStrictSwapLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictSwapLedgerViews(mutableListOf<StrictSwapLedger>(item, item)).size, "views")
    }
}
