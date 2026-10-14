package com.acme.transport.terminal.dockdoor.doorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDockDoorExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newPendingDockDoorException("code-826", "title-540")
        assertEquals(PendingDockDoorExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newPendingDockDoorException("code-826", "title-540")
        try {
            item.moveTo(PendingDockDoorExceptionStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDockDoorExceptionStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingDockDoorException("code-826", "title-540")
        item.status = PendingDockDoorExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(PendingDockDoorExceptionStatus.CREATED) }
    }
}
