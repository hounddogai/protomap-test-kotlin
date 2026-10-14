package com.acme.transport.terminal.dwell.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDwellExceptionTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalDwellException("code-576", "title-456")
        assertEquals(GlobalDwellExceptionStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newGlobalDwellException("code-576", "title-456")
        try {
            item.moveTo(GlobalDwellExceptionStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDwellExceptionStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalDwellException("code-576", "title-456")
        item.status = GlobalDwellExceptionStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalDwellExceptionStatus.DRAFT) }
    }
}
