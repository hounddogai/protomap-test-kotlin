package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingClearanceRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingClearanceRecord("code-334", "name-51")
        val view = toPendingClearanceRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingClearanceRecordViews(mutableListOf<PendingClearanceRecord>(item, item)).size, "views")
    }
}
