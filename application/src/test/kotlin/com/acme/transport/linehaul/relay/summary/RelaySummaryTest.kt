package com.acme.transport.linehaul.relay.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RelaySummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newRelaySummary("code-557", "name-561")
        assertEquals(RelaySummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newRelaySummary("code-557", "name-561")
        try {
            item.moveTo(RelaySummaryStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RelaySummaryStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRelaySummary("code-557", "name-561")
        item.status = RelaySummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(RelaySummaryStatus.PLANNED) }
    }
}
