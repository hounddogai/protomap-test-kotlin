package com.acme.transport.linehaul.trailer.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerScheduleTest {
    @Test
    fun `starts in new`() {
        val item = newTrailerSchedule("code-483", "name-776")
        assertEquals(TrailerScheduleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newTrailerSchedule("code-483", "name-776")
        try {
            item.moveTo(TrailerScheduleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerScheduleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTrailerSchedule("code-483", "name-776")
        item.status = TrailerScheduleStatus.FAILED
        assertFails("move") { item.moveTo(TrailerScheduleStatus.NEW) }
    }
}
