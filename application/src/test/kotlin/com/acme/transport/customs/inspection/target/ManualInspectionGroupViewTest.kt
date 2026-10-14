package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualInspectionGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualInspectionGroup("code-394", "title-419")
        val view = toManualInspectionGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualInspectionGroupViews(mutableListOf<ManualInspectionGroup>(item, item)).size, "views")
    }
}
