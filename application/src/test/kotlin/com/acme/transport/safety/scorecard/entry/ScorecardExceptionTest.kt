package com.acme.transport.safety.scorecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newScorecardException("code-729", "title-539")
        assertEquals(ScorecardExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newScorecardException("code-729", "title-539")
        try {
            item.moveTo(ScorecardExceptionStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardExceptionStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScorecardException("code-729", "title-539")
        item.status = ScorecardExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(ScorecardExceptionStatus.PLANNED) }
    }
}
