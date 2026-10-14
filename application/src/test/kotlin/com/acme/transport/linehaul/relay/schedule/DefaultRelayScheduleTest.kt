package com.acme.transport.linehaul.relay.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRelayScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultRelaySchedule("code-371", "title-85")
        assertEquals(DefaultRelayScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultRelaySchedule("code-371", "title-85")
        try {
            item.moveTo(DefaultRelayScheduleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRelayScheduleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultRelaySchedule("code-371", "title-85")
        item.status = DefaultRelayScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultRelayScheduleStatus.IDLE) }
    }
}
