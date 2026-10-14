package com.acme.transport.crewing.availability.defaultavailabilitysegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictAvailabilityExceptionTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictAvailabilityException("code-58", "label-218")
        assertEquals(StrictAvailabilityExceptionStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newStrictAvailabilityException("code-58", "label-218")
        try {
            item.moveTo(StrictAvailabilityExceptionStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictAvailabilityExceptionStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictAvailabilityException("code-58", "label-218")
        item.status = StrictAvailabilityExceptionStatus.CLOSED
        assertFails("move") { item.moveTo(StrictAvailabilityExceptionStatus.DRAFT) }
    }
}
