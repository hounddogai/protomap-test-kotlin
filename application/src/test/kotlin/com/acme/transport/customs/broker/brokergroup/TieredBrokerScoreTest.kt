package com.acme.transport.customs.broker.brokergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredBrokerScoreTest {
    @Test
    fun `starts in open`() {
        val item = newTieredBrokerScore("code-406", "title-686")
        assertEquals(TieredBrokerScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTieredBrokerScore("code-406", "title-686")
        try {
            item.moveTo(TieredBrokerScoreStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredBrokerScoreStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredBrokerScore("code-406", "title-686")
        item.status = TieredBrokerScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredBrokerScoreStatus.OPEN) }
    }
}
