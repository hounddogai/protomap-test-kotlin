package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newBrokerBatch("code-6", "label-893")
        assertEquals(BrokerBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newBrokerBatch("code-6", "label-893")
        try {
            item.moveTo(BrokerBatchStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerBatchStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBrokerBatch("code-6", "label-893")
        item.status = BrokerBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(BrokerBatchStatus.REQUESTED) }
    }
}
