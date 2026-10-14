package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDeclarationAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientDeclarationAllocation("code-973", "name-729")
        assertEquals(LenientDeclarationAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newLenientDeclarationAllocation("code-973", "name-729")
        try {
            item.moveTo(LenientDeclarationAllocationStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDeclarationAllocationStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientDeclarationAllocation("code-973", "name-729")
        item.status = LenientDeclarationAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(LenientDeclarationAllocationStatus.DRAFT) }
    }
}
