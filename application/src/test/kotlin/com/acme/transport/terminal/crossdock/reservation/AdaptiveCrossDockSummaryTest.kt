package com.acme.transport.terminal.crossdock.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCrossDockSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveCrossDockSummary("code-865", "name-300")
        assertEquals(AdaptiveCrossDockSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newAdaptiveCrossDockSummary("code-865", "name-300")
        try {
            item.moveTo(AdaptiveCrossDockSummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCrossDockSummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveCrossDockSummary("code-865", "name-300")
        item.status = AdaptiveCrossDockSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveCrossDockSummaryStatus.CREATED) }
    }
}
