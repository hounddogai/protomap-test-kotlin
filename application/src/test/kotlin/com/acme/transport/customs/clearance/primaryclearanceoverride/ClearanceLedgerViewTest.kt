package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceLedger("code-72", "name-391")
        val view = toClearanceLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceLedgerViews(mutableListOf<ClearanceLedger>(item, item)).size, "views")
    }
}
