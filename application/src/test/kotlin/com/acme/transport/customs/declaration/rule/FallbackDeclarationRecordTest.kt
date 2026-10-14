package com.acme.transport.customs.declaration.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDeclarationRecordTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackDeclarationRecord("code-520", "label-324")
        assertEquals(FallbackDeclarationRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newFallbackDeclarationRecord("code-520", "label-324")
        try {
            item.moveTo(FallbackDeclarationRecordStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDeclarationRecordStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackDeclarationRecord("code-520", "label-324")
        item.status = FallbackDeclarationRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackDeclarationRecordStatus.OPEN) }
    }
}
