package com.acme.transport.freightpay.dispute.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class DisputeScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDisputeSchedule("code-859", "label-270")
        val view = toDisputeScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDisputeScheduleViews(mutableListOf<DisputeSchedule>(item, item)).size, "views")
    }
}
