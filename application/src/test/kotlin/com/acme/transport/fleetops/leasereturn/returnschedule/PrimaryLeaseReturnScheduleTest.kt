package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryLeaseReturnScheduleTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryLeaseReturnSchedule("code-624", "label-196")
        assertEquals(PrimaryLeaseReturnScheduleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryLeaseReturnSchedule("code-624", "label-196")
        try {
            item.moveTo(PrimaryLeaseReturnScheduleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryLeaseReturnScheduleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryLeaseReturnSchedule("code-624", "label-196")
        item.status = PrimaryLeaseReturnScheduleStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryLeaseReturnScheduleStatus.CREATED) }
    }
}
