package com.acme.transport.linehaul.trailer.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTrailerSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveTrailerSummary("code-976", "name-768")
        assertEquals(AdaptiveTrailerSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newAdaptiveTrailerSummary("code-976", "name-768")
        try {
            item.moveTo(AdaptiveTrailerSummaryStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTrailerSummaryStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveTrailerSummary("code-976", "name-768")
        item.status = AdaptiveTrailerSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveTrailerSummaryStatus.PLANNED) }
    }
}
