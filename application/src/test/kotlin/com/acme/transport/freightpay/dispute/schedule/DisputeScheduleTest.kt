package com.acme.transport.freightpay.dispute.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeScheduleTest {
    @Test
    fun `starts in planned`() {
        val item = newDisputeSchedule("code-859", "label-270")
        assertEquals(DisputeScheduleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newDisputeSchedule("code-859", "label-270")
        try {
            item.moveTo(DisputeScheduleStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeScheduleStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDisputeSchedule("code-859", "label-270")
        item.status = DisputeScheduleStatus.CANCELLED
        assertFails("move") { item.moveTo(DisputeScheduleStatus.PLANNED) }
    }
}
