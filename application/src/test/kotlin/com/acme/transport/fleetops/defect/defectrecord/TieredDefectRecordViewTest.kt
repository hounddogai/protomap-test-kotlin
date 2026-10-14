package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredDefectRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredDefectRecord("code-326", "title-202")
        val view = toTieredDefectRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredDefectRecordViews(mutableListOf<TieredDefectRecord>(item, item)).size, "views")
    }
}
