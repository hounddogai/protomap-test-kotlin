package com.acme.transport.lastmile.redelivery.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRedeliveryScheduleTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalRedeliverySchedule("code-537", "name-869")
        assertEquals(SeasonalRedeliveryScheduleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSeasonalRedeliverySchedule("code-537", "name-869")
        try {
            item.moveTo(SeasonalRedeliveryScheduleStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRedeliveryScheduleStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalRedeliverySchedule("code-537", "name-869")
        item.status = SeasonalRedeliveryScheduleStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalRedeliveryScheduleStatus.PLANNED) }
    }
}
