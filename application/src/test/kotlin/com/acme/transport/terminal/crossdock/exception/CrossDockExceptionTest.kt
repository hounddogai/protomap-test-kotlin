package com.acme.transport.terminal.crossdock.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newCrossDockException("code-334", "name-582")
        assertEquals(CrossDockExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newCrossDockException("code-334", "name-582")
        try {
            item.moveTo(CrossDockExceptionStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockExceptionStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCrossDockException("code-334", "name-582")
        item.status = CrossDockExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(CrossDockExceptionStatus.PLANNED) }
    }
}
