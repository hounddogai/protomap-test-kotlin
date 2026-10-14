package com.acme.transport.customs.clearance.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newClearanceManifest("code-760", "name-387")
        assertEquals(ClearanceManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newClearanceManifest("code-760", "name-387")
        try {
            item.moveTo(ClearanceManifestStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceManifestStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newClearanceManifest("code-760", "name-387")
        item.status = ClearanceManifestStatus.STOPPED
        assertFails("move") { item.moveTo(ClearanceManifestStatus.IDLE) }
    }
}
