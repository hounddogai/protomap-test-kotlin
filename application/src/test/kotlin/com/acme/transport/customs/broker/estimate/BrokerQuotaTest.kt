package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newBrokerQuota("code-943", "name-29")
        assertEquals(BrokerQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newBrokerQuota("code-943", "name-29")
        try {
            item.moveTo(BrokerQuotaStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerQuotaStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBrokerQuota("code-943", "name-29")
        item.status = BrokerQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BrokerQuotaStatus.PROPOSED) }
    }
}
