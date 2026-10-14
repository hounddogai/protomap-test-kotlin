package com.acme.transport.fleetops.tire.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class TireRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireRecord("code-679", "label-534")
        val view = toTireRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireRecordViews(mutableListOf<TireRecord>(item, item)).size, "views")
    }
}
