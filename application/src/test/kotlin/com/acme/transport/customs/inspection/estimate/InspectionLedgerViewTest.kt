package com.acme.transport.customs.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionLedger("code-163", "name-611")
        val view = toInspectionLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionLedgerViews(mutableListOf<InspectionLedger>(item, item)).size, "views")
    }
}
