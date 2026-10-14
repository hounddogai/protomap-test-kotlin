package com.acme.transport.terminal.traileryard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newTrailerYardSummary("code-578", "name-481")
        assertEquals(TrailerYardSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTrailerYardSummary("code-578", "name-481")
        try {
            item.moveTo(TrailerYardSummaryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardSummaryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTrailerYardSummary("code-578", "name-481")
        item.status = TrailerYardSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(TrailerYardSummaryStatus.REQUESTED) }
    }
}
