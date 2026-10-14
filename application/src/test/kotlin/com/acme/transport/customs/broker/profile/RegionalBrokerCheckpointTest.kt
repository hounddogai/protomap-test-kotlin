package com.acme.transport.customs.broker.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalBrokerCheckpointTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalBrokerCheckpoint("code-884", "label-124")
        assertEquals(RegionalBrokerCheckpointStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRegionalBrokerCheckpoint("code-884", "label-124")
        try {
            item.moveTo(RegionalBrokerCheckpointStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalBrokerCheckpointStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalBrokerCheckpoint("code-884", "label-124")
        item.status = RegionalBrokerCheckpointStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalBrokerCheckpointStatus.DRAFT) }
    }
}
