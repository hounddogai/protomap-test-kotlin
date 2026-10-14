package com.acme.transport.terminal.dockdoor.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDockDoorMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledDockDoorMapping("code-515", "name-743")
        assertEquals(ScheduledDockDoorMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newScheduledDockDoorMapping("code-515", "name-743")
        try {
            item.moveTo(ScheduledDockDoorMappingStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDockDoorMappingStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledDockDoorMapping("code-515", "name-743")
        item.status = ScheduledDockDoorMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledDockDoorMappingStatus.PLANNED) }
    }
}
