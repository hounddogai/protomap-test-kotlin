package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCoachingScheduleTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalCoachingSchedule("code-467", "label-341")
        assertEquals(GlobalCoachingScheduleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newGlobalCoachingSchedule("code-467", "label-341")
        try {
            item.moveTo(GlobalCoachingScheduleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCoachingScheduleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalCoachingSchedule("code-467", "label-341")
        item.status = GlobalCoachingScheduleStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalCoachingScheduleStatus.OPEN) }
    }
}
