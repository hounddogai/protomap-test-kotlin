package com.acme.transport.customs.broker.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newBrokerSummary("code-279", "name-202")
        assertEquals(BrokerSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newBrokerSummary("code-279", "name-202")
        try {
            item.moveTo(BrokerSummaryStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerSummaryStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBrokerSummary("code-279", "name-202")
        item.status = BrokerSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(BrokerSummaryStatus.CREATED) }
    }
}
