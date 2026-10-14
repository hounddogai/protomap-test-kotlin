package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictNearMissScheduleTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictNearMissSchedule("code-545", "name-797")
        assertEquals(StrictNearMissScheduleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newStrictNearMissSchedule("code-545", "name-797")
        try {
            item.moveTo(StrictNearMissScheduleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictNearMissScheduleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictNearMissSchedule("code-545", "name-797")
        item.status = StrictNearMissScheduleStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictNearMissScheduleStatus.PLANNED) }
    }
}
