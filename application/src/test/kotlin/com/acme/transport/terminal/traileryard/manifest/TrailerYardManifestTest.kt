package com.acme.transport.terminal.traileryard.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newTrailerYardManifest("code-105", "title-890")
        assertEquals(TrailerYardManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTrailerYardManifest("code-105", "title-890")
        try {
            item.moveTo(TrailerYardManifestStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardManifestStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTrailerYardManifest("code-105", "title-890")
        item.status = TrailerYardManifestStatus.STOPPED
        assertFails("move") { item.moveTo(TrailerYardManifestStatus.IDLE) }
    }
}
