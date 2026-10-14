package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newDeclarationAllocation("code-952", "name-816")
        assertEquals(DeclarationAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDeclarationAllocation("code-952", "name-816")
        try {
            item.moveTo(DeclarationAllocationStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationAllocationStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDeclarationAllocation("code-952", "name-816")
        item.status = DeclarationAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(DeclarationAllocationStatus.DRAFT) }
    }
}
