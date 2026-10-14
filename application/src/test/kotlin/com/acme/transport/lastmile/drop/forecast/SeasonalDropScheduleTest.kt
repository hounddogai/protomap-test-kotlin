package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDropScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalDropSchedule("code-329", "label-729")
        assertEquals(SeasonalDropScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newSeasonalDropSchedule("code-329", "label-729")
        try {
            item.moveTo(SeasonalDropScheduleStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDropScheduleStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalDropSchedule("code-329", "label-729")
        item.status = SeasonalDropScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalDropScheduleStatus.DRAFT) }
    }
}
