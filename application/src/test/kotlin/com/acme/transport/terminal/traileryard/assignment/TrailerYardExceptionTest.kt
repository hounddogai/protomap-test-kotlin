package com.acme.transport.terminal.traileryard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newTrailerYardException("code-332", "label-898")
        assertEquals(TrailerYardExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTrailerYardException("code-332", "label-898")
        try {
            item.moveTo(TrailerYardExceptionStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardExceptionStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTrailerYardException("code-332", "label-898")
        item.status = TrailerYardExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(TrailerYardExceptionStatus.PLANNED) }
    }
}
