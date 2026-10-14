package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedHoldScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedHoldSchedule("code-889", "label-491")
        val view = toBatchedHoldScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedHoldScheduleViews(mutableListOf<BatchedHoldSchedule>(item, item)).size, "views")
    }
}
