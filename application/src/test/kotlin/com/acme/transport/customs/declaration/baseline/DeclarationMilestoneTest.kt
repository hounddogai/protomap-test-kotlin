package com.acme.transport.customs.declaration.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newDeclarationMilestone("code-638", "label-942")
        assertEquals(DeclarationMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newDeclarationMilestone("code-638", "label-942")
        try {
            item.moveTo(DeclarationMilestoneStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationMilestoneStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDeclarationMilestone("code-638", "label-942")
        item.status = DeclarationMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(DeclarationMilestoneStatus.DRAFT) }
    }
}
