package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTransitTimeManifestTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultTransitTimeManifest("code-777", "title-67")
        assertEquals(DefaultTransitTimeManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDefaultTransitTimeManifest("code-777", "title-67")
        try {
            item.moveTo(DefaultTransitTimeManifestStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTransitTimeManifestStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultTransitTimeManifest("code-777", "title-67")
        item.status = DefaultTransitTimeManifestStatus.FAILED
        assertFails("move") { item.moveTo(DefaultTransitTimeManifestStatus.NEW) }
    }
}
