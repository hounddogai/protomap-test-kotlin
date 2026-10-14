package com.acme.transport.fleetops.utilization.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationRecord("code-414", "label-759")
        val view = toUtilizationRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationRecordViews(mutableListOf<UtilizationRecord>(item, item)).size, "views")
    }
}
