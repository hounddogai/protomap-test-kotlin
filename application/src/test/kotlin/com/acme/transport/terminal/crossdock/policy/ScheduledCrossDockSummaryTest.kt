package com.acme.transport.terminal.crossdock.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCrossDockSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newScheduledCrossDockSummary("code-262", "label-527")
        assertEquals(ScheduledCrossDockSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newScheduledCrossDockSummary("code-262", "label-527")
        try {
            item.moveTo(ScheduledCrossDockSummaryStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCrossDockSummaryStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newScheduledCrossDockSummary("code-262", "label-527")
        item.status = ScheduledCrossDockSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(ScheduledCrossDockSummaryStatus.REQUESTED) }
    }
}
