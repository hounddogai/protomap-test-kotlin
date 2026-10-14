package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTenderRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTenderRecord("code-528", "title-773")
        val view = toLegacyTenderRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTenderRecordViews(mutableListOf<LegacyTenderRecord>(item, item)).size, "views")
    }
}
