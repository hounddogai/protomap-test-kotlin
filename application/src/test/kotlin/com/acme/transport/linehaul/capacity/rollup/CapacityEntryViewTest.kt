package com.acme.transport.linehaul.capacity.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityEntry("code-531", "title-115")
        val view = toCapacityEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityEntryViews(mutableListOf<CapacityEntry>(item, item)).size, "views")
    }
}
