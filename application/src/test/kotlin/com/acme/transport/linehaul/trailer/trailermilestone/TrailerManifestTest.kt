package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerManifestTest {
    @Test
    fun `starts in new`() {
        val item = newTrailerManifest("code-806", "title-900")
        assertEquals(TrailerManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTrailerManifest("code-806", "title-900")
        try {
            item.moveTo(TrailerManifestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerManifestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTrailerManifest("code-806", "title-900")
        item.status = TrailerManifestStatus.FAILED
        assertFails("move") { item.moveTo(TrailerManifestStatus.NEW) }
    }
}
