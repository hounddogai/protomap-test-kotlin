package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDeclarationManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyDeclarationManifest("code-325", "label-200")
        assertEquals(LegacyDeclarationManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLegacyDeclarationManifest("code-325", "label-200")
        try {
            item.moveTo(LegacyDeclarationManifestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDeclarationManifestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyDeclarationManifest("code-325", "label-200")
        item.status = LegacyDeclarationManifestStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyDeclarationManifestStatus.IDLE) }
    }
}
