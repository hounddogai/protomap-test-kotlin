package com.acme.transport.linehaul.relay.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelayScheduleTest {
    @Test
    fun `starts in requested`() {
        val item = newRelaySchedule("code-171", "label-498")
        assertEquals(RelayScheduleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRelaySchedule("code-171", "label-498")
        try {
            item.moveTo(RelayScheduleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelayScheduleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRelaySchedule("code-171", "label-498")
        item.status = RelayScheduleStatus.EXPIRED
        assertFails("move") { item.moveTo(RelayScheduleStatus.REQUESTED) }
    }
}
