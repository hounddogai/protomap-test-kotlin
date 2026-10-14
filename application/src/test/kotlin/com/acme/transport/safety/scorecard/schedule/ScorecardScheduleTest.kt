package com.acme.transport.safety.scorecard.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newScorecardSchedule("code-826", "name-588")
        assertEquals(ScorecardScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newScorecardSchedule("code-826", "name-588")
        try {
            item.moveTo(ScorecardScheduleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardScheduleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScorecardSchedule("code-826", "name-588")
        item.status = ScorecardScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(ScorecardScheduleStatus.IDLE) }
    }
}
