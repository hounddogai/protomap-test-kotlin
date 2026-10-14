package com.acme.transport.terminal.traileryard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTrailerYardManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyTrailerYardManifest("code-452", "name-43")
        assertEquals(LegacyTrailerYardManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newLegacyTrailerYardManifest("code-452", "name-43")
        try {
            item.moveTo(LegacyTrailerYardManifestStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTrailerYardManifestStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyTrailerYardManifest("code-452", "name-43")
        item.status = LegacyTrailerYardManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyTrailerYardManifestStatus.PROPOSED) }
    }
}
