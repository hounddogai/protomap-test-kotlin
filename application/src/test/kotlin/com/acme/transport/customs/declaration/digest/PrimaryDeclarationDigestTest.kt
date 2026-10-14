package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDeclarationDigestTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryDeclarationDigest("code-133", "title-116")
        assertEquals(PrimaryDeclarationDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPrimaryDeclarationDigest("code-133", "title-116")
        try {
            item.moveTo(PrimaryDeclarationDigestStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDeclarationDigestStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryDeclarationDigest("code-133", "title-116")
        item.status = PrimaryDeclarationDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryDeclarationDigestStatus.OPEN) }
    }
}
