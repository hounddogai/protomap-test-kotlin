package com.acme.transport.safety.scorecard.scorecardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardSummaryTest {
    @Test
    fun `starts in idle`() {
        val item = newScorecardSummary("code-580", "name-967")
        assertEquals(ScorecardSummaryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newScorecardSummary("code-580", "name-967")
        try {
            item.moveTo(ScorecardSummaryStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardSummaryStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScorecardSummary("code-580", "name-967")
        item.status = ScorecardSummaryStatus.STOPPED
        assertFails("move") { item.moveTo(ScorecardSummaryStatus.IDLE) }
    }
}
