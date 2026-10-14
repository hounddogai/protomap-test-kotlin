package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingBrokerScoreTest {
    @Test
    fun `starts in created`() {
        val item = newPendingBrokerScore("code-546", "name-706")
        assertEquals(PendingBrokerScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPendingBrokerScore("code-546", "name-706")
        try {
            item.moveTo(PendingBrokerScoreStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingBrokerScoreStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingBrokerScore("code-546", "name-706")
        item.status = PendingBrokerScoreStatus.RETIRED
        assertFails("move") { item.moveTo(PendingBrokerScoreStatus.CREATED) }
    }
}
