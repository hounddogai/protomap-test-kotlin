package com.acme.transport.terminal.dockdoor.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDockDoorNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledDockDoorNotice("code-764", "title-872")
        assertEquals(ScheduledDockDoorNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newScheduledDockDoorNotice("code-764", "title-872")
        try {
            item.moveTo(ScheduledDockDoorNoticeStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDockDoorNoticeStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledDockDoorNotice("code-764", "title-872")
        item.status = ScheduledDockDoorNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledDockDoorNoticeStatus.IDLE) }
    }
}
