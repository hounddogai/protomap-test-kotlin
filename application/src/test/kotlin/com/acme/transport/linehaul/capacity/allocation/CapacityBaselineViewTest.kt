package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityBaseline("code-986", "title-343")
        val view = toCapacityBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityBaselineViews(mutableListOf<CapacityBaseline>(item, item)).size, "views")
    }
}
