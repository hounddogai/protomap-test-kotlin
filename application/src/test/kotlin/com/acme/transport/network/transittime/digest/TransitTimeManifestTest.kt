package com.acme.transport.network.transittime.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeManifestTest {
    @Test
    fun `starts in new`() {
        val item = newTransitTimeManifest("code-609", "title-850")
        assertEquals(TransitTimeManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newTransitTimeManifest("code-609", "title-850")
        try {
            item.moveTo(TransitTimeManifestStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeManifestStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTransitTimeManifest("code-609", "title-850")
        item.status = TransitTimeManifestStatus.FAILED
        assertFails("move") { item.moveTo(TransitTimeManifestStatus.NEW) }
    }
}
