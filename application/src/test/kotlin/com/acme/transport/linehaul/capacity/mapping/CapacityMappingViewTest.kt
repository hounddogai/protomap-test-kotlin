package com.acme.transport.linehaul.capacity.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityMapping("code-478", "title-988")
        val view = toCapacityMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityMappingViews(mutableListOf<CapacityMapping>(item, item)).size, "views")
    }
}
