package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newCrossDockSummary("code-686", "label-499")
        assertEquals(CrossDockSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newCrossDockSummary("code-686", "label-499")
        try {
            item.moveTo(CrossDockSummaryStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockSummaryStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCrossDockSummary("code-686", "label-499")
        item.status = CrossDockSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(CrossDockSummaryStatus.PLANNED) }
    }
}
