package com.acme.transport.customs.declaration.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDeclarationOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryDeclarationOverride("code-904", "title-67")
        assertEquals(PrimaryDeclarationOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPrimaryDeclarationOverride("code-904", "title-67")
        try {
            item.moveTo(PrimaryDeclarationOverrideStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDeclarationOverrideStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryDeclarationOverride("code-904", "title-67")
        item.status = PrimaryDeclarationOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryDeclarationOverrideStatus.PLANNED) }
    }
}
