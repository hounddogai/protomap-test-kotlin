package com.acme.transport.linehaul.capacity.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityAllocation("code-228", "label-191")
        val view = toCapacityAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityAllocationViews(mutableListOf<CapacityAllocation>(item, item)).size, "views")
    }
}
