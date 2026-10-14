package com.acme.transport.safety.nearmiss.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledNearMissScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledNearMissSchedule("code-320", "label-579")
        assertEquals(ScheduledNearMissScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newScheduledNearMissSchedule("code-320", "label-579")
        try {
            item.moveTo(ScheduledNearMissScheduleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledNearMissScheduleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledNearMissSchedule("code-320", "label-579")
        item.status = ScheduledNearMissScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledNearMissScheduleStatus.REQUESTED) }
    }
}
