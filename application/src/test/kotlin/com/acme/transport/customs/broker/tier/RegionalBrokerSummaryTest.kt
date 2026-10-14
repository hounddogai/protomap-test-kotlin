package com.acme.transport.customs.broker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalBrokerSummaryTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalBrokerSummary("code-340", "name-64")
        assertEquals(RegionalBrokerSummaryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newRegionalBrokerSummary("code-340", "name-64")
        try {
            item.moveTo(RegionalBrokerSummaryStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalBrokerSummaryStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalBrokerSummary("code-340", "name-64")
        item.status = RegionalBrokerSummaryStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalBrokerSummaryStatus.DRAFT) }
    }
}
