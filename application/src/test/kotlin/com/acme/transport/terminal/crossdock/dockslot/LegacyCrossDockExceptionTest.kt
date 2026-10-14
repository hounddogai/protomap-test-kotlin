package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCrossDockExceptionTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyCrossDockException("code-268", "title-239")
        assertEquals(LegacyCrossDockExceptionStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLegacyCrossDockException("code-268", "title-239")
        try {
            item.moveTo(LegacyCrossDockExceptionStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCrossDockExceptionStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyCrossDockException("code-268", "title-239")
        item.status = LegacyCrossDockExceptionStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyCrossDockExceptionStatus.DRAFT) }
    }
}
