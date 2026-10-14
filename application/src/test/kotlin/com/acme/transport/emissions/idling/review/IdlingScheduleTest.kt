package com.acme.transport.emissions.idling.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newIdlingSchedule("code-917", "name-14")
        assertEquals(IdlingScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newIdlingSchedule("code-917", "name-14")
        try {
            item.moveTo(IdlingScheduleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingScheduleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newIdlingSchedule("code-917", "name-14")
        item.status = IdlingScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(IdlingScheduleStatus.PROPOSED) }
    }
}
