package com.acme.transport.customs.broker.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerSnapshotTest {
    @Test
    fun `starts in idle`() {
        val item = newBrokerSnapshot("code-701", "label-845")
        assertEquals(BrokerSnapshotStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newBrokerSnapshot("code-701", "label-845")
        try {
            item.moveTo(BrokerSnapshotStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerSnapshotStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBrokerSnapshot("code-701", "label-845")
        item.status = BrokerSnapshotStatus.STOPPED
        assertFails("move") { item.moveTo(BrokerSnapshotStatus.IDLE) }
    }
}
