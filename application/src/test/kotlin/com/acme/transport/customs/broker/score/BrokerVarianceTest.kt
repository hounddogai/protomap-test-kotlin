package com.acme.transport.customs.broker.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newBrokerVariance("code-734", "title-533")
        assertEquals(BrokerVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newBrokerVariance("code-734", "title-533")
        try {
            item.moveTo(BrokerVarianceStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerVarianceStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBrokerVariance("code-734", "title-533")
        item.status = BrokerVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BrokerVarianceStatus.PROPOSED) }
    }
}
