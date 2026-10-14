package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationClaimTest {
    @Test
    fun `starts in open`() {
        val item = newDeclarationClaim("code-283", "name-141")
        assertEquals(DeclarationClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newDeclarationClaim("code-283", "name-141")
        try {
            item.moveTo(DeclarationClaimStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationClaimStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDeclarationClaim("code-283", "name-141")
        item.status = DeclarationClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(DeclarationClaimStatus.OPEN) }
    }
}
