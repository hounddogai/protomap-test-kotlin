package com.acme.transport.terminal.dockdoor.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedDockDoorExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedDockDoorException("code-23", "title-442")
        assertEquals(CappedDockDoorExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newCappedDockDoorException("code-23", "title-442")
        try {
            item.moveTo(CappedDockDoorExceptionStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedDockDoorExceptionStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedDockDoorException("code-23", "title-442")
        item.status = CappedDockDoorExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(CappedDockDoorExceptionStatus.IDLE) }
    }
}
