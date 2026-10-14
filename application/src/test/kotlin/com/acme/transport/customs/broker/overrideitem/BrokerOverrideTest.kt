package com.acme.transport.customs.broker.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newBrokerOverride("code-654", "label-797")
        assertEquals(BrokerOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newBrokerOverride("code-654", "label-797")
        try {
            item.moveTo(BrokerOverrideStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerOverrideStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBrokerOverride("code-654", "label-797")
        item.status = BrokerOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(BrokerOverrideStatus.DRAFT) }
    }
}
