package com.acme.transport.linehaul.trailer.trailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTrailerManifestTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveTrailerManifest("code-834", "title-288")
        assertEquals(AdaptiveTrailerManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newAdaptiveTrailerManifest("code-834", "title-288")
        try {
            item.moveTo(AdaptiveTrailerManifestStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTrailerManifestStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveTrailerManifest("code-834", "title-288")
        item.status = AdaptiveTrailerManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveTrailerManifestStatus.OPEN) }
    }
}
