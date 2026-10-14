package com.acme.transport.safety.coaching.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCoachingScheduleTest {
    @Test
    fun `starts in new`() {
        val item = newStrictCoachingSchedule("code-601", "title-670")
        assertEquals(StrictCoachingScheduleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newStrictCoachingSchedule("code-601", "title-670")
        try {
            item.moveTo(StrictCoachingScheduleStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCoachingScheduleStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictCoachingSchedule("code-601", "title-670")
        item.status = StrictCoachingScheduleStatus.FAILED
        assertFails("move") { item.moveTo(StrictCoachingScheduleStatus.NEW) }
    }
}
