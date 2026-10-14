package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTenderRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledTenderRecord("code-753", "title-343")
        val view = toScheduledTenderRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledTenderRecordViews(mutableListOf<ScheduledTenderRecord>(item, item)).size, "views")
    }
}
