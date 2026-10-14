package com.acme.transport.terminal.traileryard.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTrailerYardManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalTrailerYardManifest("code-185", "label-800")
        assertEquals(RegionalTrailerYardManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newRegionalTrailerYardManifest("code-185", "label-800")
        try {
            item.moveTo(RegionalTrailerYardManifestStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTrailerYardManifestStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalTrailerYardManifest("code-185", "label-800")
        item.status = RegionalTrailerYardManifestStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalTrailerYardManifestStatus.IDLE) }
    }
}
