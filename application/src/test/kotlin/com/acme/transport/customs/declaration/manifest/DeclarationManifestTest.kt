package com.acme.transport.customs.declaration.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newDeclarationManifest("code-550", "title-890")
        assertEquals(DeclarationManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDeclarationManifest("code-550", "title-890")
        try {
            item.moveTo(DeclarationManifestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationManifestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDeclarationManifest("code-550", "title-890")
        item.status = DeclarationManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DeclarationManifestStatus.PROPOSED) }
    }
}
