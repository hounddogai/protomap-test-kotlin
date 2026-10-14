package com.acme.transport.customs.broker.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingBrokerScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingBrokerScore("code-659", "title-671")
        assertEquals(RollingBrokerScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newRollingBrokerScore("code-659", "title-671")
        try {
            item.moveTo(RollingBrokerScoreStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingBrokerScoreStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingBrokerScore("code-659", "title-671")
        item.status = RollingBrokerScoreStatus.CLOSED
        assertFails("move") { item.moveTo(RollingBrokerScoreStatus.DRAFT) }
    }
}
