package com.acme.transport.customs.declaration.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationDigestTest {
    @Test
    fun `starts in new`() {
        val item = newDeclarationDigest("code-349", "name-930")
        assertEquals(DeclarationDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDeclarationDigest("code-349", "name-930")
        try {
            item.moveTo(DeclarationDigestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationDigestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDeclarationDigest("code-349", "name-930")
        item.status = DeclarationDigestStatus.FAILED
        assertFails("move") { item.moveTo(DeclarationDigestStatus.NEW) }
    }
}
