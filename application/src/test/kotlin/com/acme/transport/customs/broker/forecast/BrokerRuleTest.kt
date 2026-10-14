package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newBrokerRule("code-383", "name-71")
        assertEquals(BrokerRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newBrokerRule("code-383", "name-71")
        try {
            item.moveTo(BrokerRuleStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerRuleStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBrokerRule("code-383", "name-71")
        item.status = BrokerRuleStatus.CLOSED
        assertFails("move") { item.moveTo(BrokerRuleStatus.DRAFT) }
    }
}
