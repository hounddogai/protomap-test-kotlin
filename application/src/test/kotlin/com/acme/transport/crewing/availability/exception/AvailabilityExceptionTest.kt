package com.acme.transport.crewing.availability.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityExceptionTest {
    @Test
    fun `starts in draft`() {
        val item = newAvailabilityException("code-355", "title-408")
        assertEquals(AvailabilityExceptionStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newAvailabilityException("code-355", "title-408")
        try {
            item.moveTo(AvailabilityExceptionStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityExceptionStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAvailabilityException("code-355", "title-408")
        item.status = AvailabilityExceptionStatus.CLOSED
        assertFails("move") { item.moveTo(AvailabilityExceptionStatus.DRAFT) }
    }
}
