package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerMilestoneTest {
    @Test
    fun `starts in proposed`() {
        val item = newBrokerMilestone("code-590", "name-284")
        assertEquals(BrokerMilestoneStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newBrokerMilestone("code-590", "name-284")
        try {
            item.moveTo(BrokerMilestoneStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerMilestoneStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBrokerMilestone("code-590", "name-284")
        item.status = BrokerMilestoneStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BrokerMilestoneStatus.PROPOSED) }
    }
}
