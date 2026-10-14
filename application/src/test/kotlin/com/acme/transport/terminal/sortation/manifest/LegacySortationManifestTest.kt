package com.acme.transport.terminal.sortation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacySortationManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacySortationManifest("code-51", "name-462")
        assertEquals(LegacySortationManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacySortationManifest("code-51", "name-462")
        try {
            item.moveTo(LegacySortationManifestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacySortationManifestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacySortationManifest("code-51", "name-462")
        item.status = LegacySortationManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacySortationManifestStatus.PLANNED) }
    }
}
