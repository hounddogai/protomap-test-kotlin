package com.acme.transport.fleetops.tire.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTireRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTireRecord("code-476", "label-53")
        val view = toStrictTireRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTireRecordViews(mutableListOf<StrictTireRecord>(item, item)).size, "views")
    }
}
