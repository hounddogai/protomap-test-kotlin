package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledCapacityAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledCapacityAllocation("code-709", "label-495")
        val view = toScheduledCapacityAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledCapacityAllocationViews(mutableListOf<ScheduledCapacityAllocation>(item, item)).size, "views")
    }
}
