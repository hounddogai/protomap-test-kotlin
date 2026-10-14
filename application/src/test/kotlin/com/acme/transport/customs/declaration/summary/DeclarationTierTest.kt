package com.acme.transport.customs.declaration.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationTierTest {
    @Test
    fun `starts in new`() {
        val item = newDeclarationTier("code-233", "title-689")
        assertEquals(DeclarationTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newDeclarationTier("code-233", "title-689")
        try {
            item.moveTo(DeclarationTierStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationTierStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDeclarationTier("code-233", "title-689")
        item.status = DeclarationTierStatus.FAILED
        assertFails("move") { item.moveTo(DeclarationTierStatus.NEW) }
    }
}
