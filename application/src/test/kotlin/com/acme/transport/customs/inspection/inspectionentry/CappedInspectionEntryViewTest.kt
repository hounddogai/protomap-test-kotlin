package com.acme.transport.customs.inspection.inspectionentry

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedInspectionEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedInspectionEntry("code-106", "title-259")
        val view = toCappedInspectionEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedInspectionEntryViews(mutableListOf<CappedInspectionEntry>(item, item)).size, "views")
    }
}
