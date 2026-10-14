package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedBrokerScoreTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedBrokerScore("code-656", "label-104")
        assertEquals(WeightedBrokerScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newWeightedBrokerScore("code-656", "label-104")
        try {
            item.moveTo(WeightedBrokerScoreStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedBrokerScoreStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedBrokerScore("code-656", "label-104")
        item.status = WeightedBrokerScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedBrokerScoreStatus.OPEN) }
    }
}
