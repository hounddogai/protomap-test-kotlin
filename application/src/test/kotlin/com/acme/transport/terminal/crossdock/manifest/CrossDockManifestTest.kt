package com.acme.transport.terminal.crossdock.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockManifestTest {
    @Test
    fun `starts in new`() {
        val item = newCrossDockManifest("code-314", "title-494")
        assertEquals(CrossDockManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newCrossDockManifest("code-314", "title-494")
        try {
            item.moveTo(CrossDockManifestStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockManifestStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCrossDockManifest("code-314", "title-494")
        item.status = CrossDockManifestStatus.FAILED
        assertFails("move") { item.moveTo(CrossDockManifestStatus.NEW) }
    }
}
