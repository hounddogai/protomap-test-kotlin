package com.acme.transport.network.hub.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newHubSummary("code-996", "label-836")
        assertEquals(HubSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newHubSummary("code-996", "label-836")
        try {
            item.moveTo(HubSummaryStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubSummaryStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newHubSummary("code-996", "label-836")
        item.status = HubSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(HubSummaryStatus.PROPOSED) }
    }
}
