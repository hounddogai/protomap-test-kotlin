package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorManifestTest {
    @Test
    fun `starts in open`() {
        val item = newDockDoorManifest("code-258", "label-831")
        assertEquals(DockDoorManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDockDoorManifest("code-258", "label-831")
        try {
            item.moveTo(DockDoorManifestStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorManifestStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDockDoorManifest("code-258", "label-831")
        item.status = DockDoorManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(DockDoorManifestStatus.OPEN) }
    }
}
