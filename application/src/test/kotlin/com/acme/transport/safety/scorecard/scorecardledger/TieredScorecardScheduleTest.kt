package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredScorecardScheduleTest {
    @Test
    fun `starts in created`() {
        val item = newTieredScorecardSchedule("code-628", "title-493")
        assertEquals(TieredScorecardScheduleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredScorecardSchedule("code-628", "title-493")
        try {
            item.moveTo(TieredScorecardScheduleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredScorecardScheduleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredScorecardSchedule("code-628", "title-493")
        item.status = TieredScorecardScheduleStatus.RETIRED
        assertFails("move") { item.moveTo(TieredScorecardScheduleStatus.CREATED) }
    }
}
