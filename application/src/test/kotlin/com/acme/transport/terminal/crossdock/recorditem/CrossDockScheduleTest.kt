package com.acme.transport.terminal.crossdock.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newCrossDockSchedule("code-492", "title-891")
        assertEquals(CrossDockScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newCrossDockSchedule("code-492", "title-891")
        try {
            item.moveTo(CrossDockScheduleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockScheduleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCrossDockSchedule("code-492", "title-891")
        item.status = CrossDockScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CrossDockScheduleStatus.PROPOSED) }
    }
}
