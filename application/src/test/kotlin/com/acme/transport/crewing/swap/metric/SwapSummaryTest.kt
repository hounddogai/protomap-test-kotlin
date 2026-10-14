package com.acme.transport.crewing.swap.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SwapSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newSwapSummary("code-531", "label-22")
        assertEquals(SwapSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSwapSummary("code-531", "label-22")
        try {
            item.moveTo(SwapSummaryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SwapSummaryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSwapSummary("code-531", "label-22")
        item.status = SwapSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(SwapSummaryStatus.PLANNED) }
    }
}
