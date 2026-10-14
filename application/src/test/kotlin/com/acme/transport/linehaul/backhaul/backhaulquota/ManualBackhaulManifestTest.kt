package com.acme.transport.linehaul.backhaul.backhaulquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualBackhaulManifestTest {
    @Test
    fun `starts in open`() {
        val item = newManualBackhaulManifest("code-284", "name-715")
        assertEquals(ManualBackhaulManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newManualBackhaulManifest("code-284", "name-715")
        try {
            item.moveTo(ManualBackhaulManifestStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualBackhaulManifestStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualBackhaulManifest("code-284", "name-715")
        item.status = ManualBackhaulManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualBackhaulManifestStatus.OPEN) }
    }
}
