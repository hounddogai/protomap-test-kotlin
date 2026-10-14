package com.acme.transport.fleetops.defect.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectRecord("code-731", "label-534")
        val view = toDefectRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectRecordViews(mutableListOf<DefectRecord>(item, item)).size, "views")
    }
}
