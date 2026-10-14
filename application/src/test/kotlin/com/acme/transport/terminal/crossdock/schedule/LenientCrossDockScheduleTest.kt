package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCrossDockScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientCrossDockSchedule("code-61", "name-266")
        assertEquals(LenientCrossDockScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLenientCrossDockSchedule("code-61", "name-266")
        try {
            item.moveTo(LenientCrossDockScheduleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCrossDockScheduleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientCrossDockSchedule("code-61", "name-266")
        item.status = LenientCrossDockScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientCrossDockScheduleStatus.PROPOSED) }
    }
}
