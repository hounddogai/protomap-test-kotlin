package com.acme.transport.safety.nearmiss.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissLedger("code-318", "name-997")
        val view = toNearMissLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissLedgerViews(mutableListOf<NearMissLedger>(item, item)).size, "views")
    }
}
