package com.acme.transport.terminal.dwell.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDwellSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedDwellSummary("code-696", "label-219")
        assertEquals(WeightedDwellSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newWeightedDwellSummary("code-696", "label-219")
        try {
            item.moveTo(WeightedDwellSummaryStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDwellSummaryStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedDwellSummary("code-696", "label-219")
        item.status = WeightedDwellSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedDwellSummaryStatus.OPEN) }
    }
}
