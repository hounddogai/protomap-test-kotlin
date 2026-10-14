package com.acme.transport.customs.declaration.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDeclarationRecordTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultDeclarationRecord("code-91", "label-41")
        assertEquals(DefaultDeclarationRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDefaultDeclarationRecord("code-91", "label-41")
        try {
            item.moveTo(DefaultDeclarationRecordStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDeclarationRecordStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultDeclarationRecord("code-91", "label-41")
        item.status = DefaultDeclarationRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultDeclarationRecordStatus.OPEN) }
    }
}
