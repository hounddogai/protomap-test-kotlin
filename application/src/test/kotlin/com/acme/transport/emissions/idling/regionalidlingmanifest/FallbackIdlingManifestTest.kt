package com.acme.transport.emissions.idling.regionalidlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackIdlingManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackIdlingManifest("code-384", "title-653")
        assertEquals(FallbackIdlingManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newFallbackIdlingManifest("code-384", "title-653")
        try {
            item.moveTo(FallbackIdlingManifestStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackIdlingManifestStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackIdlingManifest("code-384", "title-653")
        item.status = FallbackIdlingManifestStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackIdlingManifestStatus.DRAFT) }
    }
}
