package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalBrokerSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalBrokerSummary("code-188", "name-298")
        assertEquals(GlobalBrokerSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newGlobalBrokerSummary("code-188", "name-298")
        try {
            item.moveTo(GlobalBrokerSummaryStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalBrokerSummaryStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalBrokerSummary("code-188", "name-298")
        item.status = GlobalBrokerSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalBrokerSummaryStatus.CREATED) }
    }
}
