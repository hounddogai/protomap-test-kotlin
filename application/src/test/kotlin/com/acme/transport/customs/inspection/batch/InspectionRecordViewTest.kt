package com.acme.transport.customs.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionRecord("code-934", "title-908")
        val view = toInspectionRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionRecordViews(mutableListOf<InspectionRecord>(item, item)).size, "views")
    }
}
