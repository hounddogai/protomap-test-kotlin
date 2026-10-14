package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTrailerYardScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newManualTrailerYardSchedule("code-986", "label-60")
        assertEquals(ManualTrailerYardScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newManualTrailerYardSchedule("code-986", "label-60")
        try {
            item.moveTo(ManualTrailerYardScheduleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTrailerYardScheduleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualTrailerYardSchedule("code-986", "label-60")
        item.status = ManualTrailerYardScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualTrailerYardScheduleStatus.REQUESTED) }
    }
}
