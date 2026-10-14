package com.acme.transport.linehaul.capacity.capacityrequest

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCapacityScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCapacitySchedule("code-769", "title-502")
        val view = toBatchedCapacityScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCapacityScheduleViews(mutableListOf<BatchedCapacitySchedule>(item, item)).size, "views")
    }
}
