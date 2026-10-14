package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyInspectionLedgerViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyInspectionLedger("code-771", "title-549")
        val view = toLegacyInspectionLedgerView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyInspectionLedgerViews(mutableListOf<LegacyInspectionLedger>(item, item)).size, "views")
    }
}
