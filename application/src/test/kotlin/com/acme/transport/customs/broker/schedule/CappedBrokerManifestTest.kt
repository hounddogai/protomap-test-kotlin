package com.acme.transport.customs.broker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedBrokerManifestTest {
    @Test
    fun `starts in open`() {
        val item = newCappedBrokerManifest("code-393", "name-927")
        assertEquals(CappedBrokerManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCappedBrokerManifest("code-393", "name-927")
        try {
            item.moveTo(CappedBrokerManifestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedBrokerManifestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedBrokerManifest("code-393", "name-927")
        item.status = CappedBrokerManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedBrokerManifestStatus.OPEN) }
    }
}
