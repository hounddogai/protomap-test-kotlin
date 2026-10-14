package com.acme.transport.customs.broker.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newBrokerPolicy("code-258", "title-96")
        assertEquals(BrokerPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBrokerPolicy("code-258", "title-96")
        try {
            item.moveTo(BrokerPolicyStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerPolicyStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBrokerPolicy("code-258", "title-96")
        item.status = BrokerPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(BrokerPolicyStatus.IDLE) }
    }
}
