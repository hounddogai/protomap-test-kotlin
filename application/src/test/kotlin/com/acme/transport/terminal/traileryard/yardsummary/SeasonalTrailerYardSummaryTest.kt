package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTrailerYardSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalTrailerYardSummary("code-154", "label-836")
        assertEquals(SeasonalTrailerYardSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalTrailerYardSummary("code-154", "label-836")
        try {
            item.moveTo(SeasonalTrailerYardSummaryStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTrailerYardSummaryStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalTrailerYardSummary("code-154", "label-836")
        item.status = SeasonalTrailerYardSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalTrailerYardSummaryStatus.OPEN) }
    }
}
