package com.acme.transport.crewing.restbreak.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRestBreakScheduleTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingRestBreakSchedule("code-181", "name-296")
        assertEquals(PendingRestBreakScheduleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingRestBreakSchedule("code-181", "name-296")
        try {
            item.moveTo(PendingRestBreakScheduleStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRestBreakScheduleStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingRestBreakSchedule("code-181", "name-296")
        item.status = PendingRestBreakScheduleStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingRestBreakScheduleStatus.PLANNED) }
    }
}
