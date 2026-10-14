package com.acme.transport.customs.declaration.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDeclarationManifestTest {
    @Test
    fun `starts in open`() {
        val item = newRollingDeclarationManifest("code-538", "name-7")
        assertEquals(RollingDeclarationManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRollingDeclarationManifest("code-538", "name-7")
        try {
            item.moveTo(RollingDeclarationManifestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDeclarationManifestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingDeclarationManifest("code-538", "name-7")
        item.status = RollingDeclarationManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingDeclarationManifestStatus.OPEN) }
    }
}
