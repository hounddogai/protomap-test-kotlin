package com.acme.transport.lastmile.redelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryExceptionTest {
    @Test
    fun `starts in draft`() {
        val item = newRedeliveryException("code-343", "title-713")
        assertEquals(RedeliveryExceptionStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRedeliveryException("code-343", "title-713")
        try {
            item.moveTo(RedeliveryExceptionStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryExceptionStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRedeliveryException("code-343", "title-713")
        item.status = RedeliveryExceptionStatus.CLOSED
        assertFails("move") { item.moveTo(RedeliveryExceptionStatus.DRAFT) }
    }
}
