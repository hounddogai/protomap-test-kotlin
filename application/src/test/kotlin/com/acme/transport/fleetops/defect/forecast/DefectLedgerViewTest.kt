package com.acme.transport.fleetops.defect.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectLedger("code-244", "title-224")
        val view = toDefectLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectLedgerViews(mutableListOf<DefectLedger>(item, item)).size, "views")
    }
}
