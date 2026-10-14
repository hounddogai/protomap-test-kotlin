package com.acme.transport.terminal.dwell.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDwellScheduleTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientDwellSchedule("code-38", "label-673")
        assertEquals(LenientDwellScheduleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newLenientDwellSchedule("code-38", "label-673")
        try {
            item.moveTo(LenientDwellScheduleStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDwellScheduleStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientDwellSchedule("code-38", "label-673")
        item.status = LenientDwellScheduleStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientDwellScheduleStatus.PLANNED) }
    }
}
