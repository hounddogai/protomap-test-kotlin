package com.acme.transport.terminal.crossdock.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingCrossDockSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingCrossDockSummary("code-449", "label-72")
        assertEquals(PendingCrossDockSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingCrossDockSummary("code-449", "label-72")
        try {
            item.moveTo(PendingCrossDockSummaryStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingCrossDockSummaryStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingCrossDockSummary("code-449", "label-72")
        item.status = PendingCrossDockSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingCrossDockSummaryStatus.PLANNED) }
    }
}
