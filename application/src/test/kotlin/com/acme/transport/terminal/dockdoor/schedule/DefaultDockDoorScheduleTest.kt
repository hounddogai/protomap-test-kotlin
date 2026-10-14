package com.acme.transport.terminal.dockdoor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDockDoorScheduleTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultDockDoorSchedule("code-545", "name-306")
        assertEquals(DefaultDockDoorScheduleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultDockDoorSchedule("code-545", "name-306")
        try {
            item.moveTo(DefaultDockDoorScheduleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDockDoorScheduleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultDockDoorSchedule("code-545", "name-306")
        item.status = DefaultDockDoorScheduleStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultDockDoorScheduleStatus.CREATED) }
    }
}
