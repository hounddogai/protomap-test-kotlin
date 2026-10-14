package com.acme.transport.terminal.dockdoor.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDockDoorExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackDockDoorException("code-680", "name-354")
        assertEquals(FallbackDockDoorExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newFallbackDockDoorException("code-680", "name-354")
        try {
            item.moveTo(FallbackDockDoorExceptionStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDockDoorExceptionStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackDockDoorException("code-680", "name-354")
        item.status = FallbackDockDoorExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackDockDoorExceptionStatus.PLANNED) }
    }
}
