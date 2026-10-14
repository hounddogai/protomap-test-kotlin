package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientHoldScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientHoldSchedule("code-344", "name-518")
        assertEquals(LenientHoldScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLenientHoldSchedule("code-344", "name-518")
        try {
            item.moveTo(LenientHoldScheduleStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientHoldScheduleStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientHoldSchedule("code-344", "name-518")
        item.status = LenientHoldScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientHoldScheduleStatus.PROPOSED) }
    }
}
