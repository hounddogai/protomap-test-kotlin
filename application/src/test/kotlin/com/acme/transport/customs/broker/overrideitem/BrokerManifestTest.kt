package com.acme.transport.customs.broker.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerManifestTest {
    @Test
    fun `starts in new`() {
        val item = newBrokerManifest("code-945", "name-905")
        assertEquals(BrokerManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newBrokerManifest("code-945", "name-905")
        try {
            item.moveTo(BrokerManifestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerManifestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBrokerManifest("code-945", "name-905")
        item.status = BrokerManifestStatus.FAILED
        assertFails("move") { item.moveTo(BrokerManifestStatus.NEW) }
    }
}
