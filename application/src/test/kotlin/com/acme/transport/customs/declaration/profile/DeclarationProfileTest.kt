package com.acme.transport.customs.declaration.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationProfileTest {
    @Test
    fun `starts in open`() {
        val item = newDeclarationProfile("code-735", "title-946")
        assertEquals(DeclarationProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newDeclarationProfile("code-735", "title-946")
        try {
            item.moveTo(DeclarationProfileStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationProfileStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDeclarationProfile("code-735", "title-946")
        item.status = DeclarationProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(DeclarationProfileStatus.OPEN) }
    }
}
