package com.acme.transport.crewing.roster.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictRosterExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictRosterException("code-567", "label-242")
        assertEquals(StrictRosterExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newStrictRosterException("code-567", "label-242")
        try {
            item.moveTo(StrictRosterExceptionStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictRosterExceptionStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictRosterException("code-567", "label-242")
        item.status = StrictRosterExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictRosterExceptionStatus.PLANNED) }
    }
}
