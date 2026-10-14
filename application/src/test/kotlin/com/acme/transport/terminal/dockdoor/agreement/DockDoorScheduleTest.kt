package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorScheduleTest {
    @Test
    fun `starts in new`() {
        val item = newDockDoorSchedule("code-725", "name-592")
        assertEquals(DockDoorScheduleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDockDoorSchedule("code-725", "name-592")
        try {
            item.moveTo(DockDoorScheduleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorScheduleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDockDoorSchedule("code-725", "name-592")
        item.status = DockDoorScheduleStatus.FAILED
        assertFails("move") { item.moveTo(DockDoorScheduleStatus.NEW) }
    }
}
