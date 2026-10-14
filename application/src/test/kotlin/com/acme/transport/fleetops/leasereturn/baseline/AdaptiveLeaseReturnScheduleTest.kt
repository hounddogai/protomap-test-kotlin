package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLeaseReturnScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveLeaseReturnSchedule("code-622", "label-105")
        assertEquals(AdaptiveLeaseReturnScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveLeaseReturnSchedule("code-622", "label-105")
        try {
            item.moveTo(AdaptiveLeaseReturnScheduleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLeaseReturnScheduleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveLeaseReturnSchedule("code-622", "label-105")
        item.status = AdaptiveLeaseReturnScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveLeaseReturnScheduleStatus.DRAFT) }
    }
}
