package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCoachingScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredCoachingSchedule("code-645", "label-225")
        assertEquals(TieredCoachingScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTieredCoachingSchedule("code-645", "label-225")
        try {
            item.moveTo(TieredCoachingScheduleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCoachingScheduleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredCoachingSchedule("code-645", "label-225")
        item.status = TieredCoachingScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(TieredCoachingScheduleStatus.IDLE) }
    }
}
