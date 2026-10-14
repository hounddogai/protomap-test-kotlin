package com.acme.transport.crewing.roster.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRosterSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackRosterSummary("code-349", "label-801")
        assertEquals(FallbackRosterSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackRosterSummary("code-349", "label-801")
        try {
            item.moveTo(FallbackRosterSummaryStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRosterSummaryStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackRosterSummary("code-349", "label-801")
        item.status = FallbackRosterSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackRosterSummaryStatus.OPEN) }
    }
}
