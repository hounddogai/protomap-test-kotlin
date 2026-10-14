package com.acme.transport.customs.broker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredBrokerRequestTest {
    @Test
    fun `starts in open`() {
        val item = newTieredBrokerRequest("code-654", "label-193")
        assertEquals(TieredBrokerRequestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTieredBrokerRequest("code-654", "label-193")
        try {
            item.moveTo(TieredBrokerRequestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredBrokerRequestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredBrokerRequest("code-654", "label-193")
        item.status = TieredBrokerRequestStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredBrokerRequestStatus.OPEN) }
    }
}
