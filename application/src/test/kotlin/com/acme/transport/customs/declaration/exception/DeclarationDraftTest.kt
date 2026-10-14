package com.acme.transport.customs.declaration.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationDraftTest {
    @Test
    fun `starts in idle`() {
        val item = newDeclarationDraft("code-693", "label-628")
        assertEquals(DeclarationDraftStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDeclarationDraft("code-693", "label-628")
        try {
            item.moveTo(DeclarationDraftStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationDraftStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDeclarationDraft("code-693", "label-628")
        item.status = DeclarationDraftStatus.STOPPED
        assertFails("move") { item.moveTo(DeclarationDraftStatus.IDLE) }
    }
}
