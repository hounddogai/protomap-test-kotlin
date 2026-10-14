package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedCapacityMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedCapacityMapping("code-311", "label-680")
        val view = toCappedCapacityMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedCapacityMappingViews(mutableListOf<CappedCapacityMapping>(item, item)).size, "views")
    }
}
