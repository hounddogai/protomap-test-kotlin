package com.acme.transport.linehaul.tender.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class TenderRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTenderRecord("code-468", "title-813")
        val view = toTenderRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTenderRecordViews(mutableListOf<TenderRecord>(item, item)).size, "views")
    }
}
