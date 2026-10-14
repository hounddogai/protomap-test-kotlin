package com.acme.transport.customs.broker.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredBrokerManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredBrokerManifest("code-995", "title-809")
        assertEquals(TieredBrokerManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredBrokerManifest("code-995", "title-809")
        try {
            item.moveTo(TieredBrokerManifestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredBrokerManifestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredBrokerManifest("code-995", "title-809")
        item.status = TieredBrokerManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredBrokerManifestStatus.PROPOSED) }
    }
}
