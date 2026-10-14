package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityRecord("code-900", "name-683")
        val view = toCapacityRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityRecordViews(mutableListOf<CapacityRecord>(item, item)).size, "views")
    }
}
