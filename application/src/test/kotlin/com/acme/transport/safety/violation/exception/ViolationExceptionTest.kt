package com.acme.transport.safety.violation.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationExceptionTest {
    @Test
    fun `starts in draft`() {
        val item = newViolationException("code-452", "label-138")
        assertEquals(ViolationExceptionStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newViolationException("code-452", "label-138")
        try {
            item.moveTo(ViolationExceptionStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationExceptionStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newViolationException("code-452", "label-138")
        item.status = ViolationExceptionStatus.CLOSED
        assertFails("move") { item.moveTo(ViolationExceptionStatus.DRAFT) }
    }
}
