package com.acme.transport.terminal.dockdoor.doorrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDockDoorRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledDockDoorRecord("code-858", "name-439")
        assertEquals(ScheduledDockDoorRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newScheduledDockDoorRecord("code-858", "name-439")
        try {
            item.moveTo(ScheduledDockDoorRecordStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDockDoorRecordStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledDockDoorRecord("code-858", "name-439")
        item.status = ScheduledDockDoorRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledDockDoorRecordStatus.REQUESTED) }
    }
}
