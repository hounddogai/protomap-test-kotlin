package com.acme.transport.network.cutoff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffManifestTest {
    @Test
    fun `starts in open`() {
        val item = newCutoffManifest("code-106", "name-544")
        assertEquals(CutoffManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCutoffManifest("code-106", "name-544")
        try {
            item.moveTo(CutoffManifestStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffManifestStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCutoffManifest("code-106", "name-544")
        item.status = CutoffManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(CutoffManifestStatus.OPEN) }
    }
}
