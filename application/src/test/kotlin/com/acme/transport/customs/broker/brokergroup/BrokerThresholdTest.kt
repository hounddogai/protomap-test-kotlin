package com.acme.transport.customs.broker.brokergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerThresholdTest {
    @Test
    fun `starts in created`() {
        val item = newBrokerThreshold("code-84", "title-238")
        assertEquals(BrokerThresholdStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBrokerThreshold("code-84", "title-238")
        try {
            item.moveTo(BrokerThresholdStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerThresholdStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBrokerThreshold("code-84", "title-238")
        item.status = BrokerThresholdStatus.RETIRED
        assertFails("move") { item.moveTo(BrokerThresholdStatus.CREATED) }
    }
}
