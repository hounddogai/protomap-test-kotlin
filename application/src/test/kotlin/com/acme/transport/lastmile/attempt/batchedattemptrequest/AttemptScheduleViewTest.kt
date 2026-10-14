package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptSchedule("code-633", "name-498")
        val view = toAttemptScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptScheduleViews(mutableListOf<AttemptSchedule>(item, item)).size, "views")
    }
}
