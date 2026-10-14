package com.acme.transport.customs.declaration.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newDeclarationAssignment("code-549", "title-776")
        assertEquals(DeclarationAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newDeclarationAssignment("code-549", "title-776")
        try {
            item.moveTo(DeclarationAssignmentStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationAssignmentStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDeclarationAssignment("code-549", "title-776")
        item.status = DeclarationAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(DeclarationAssignmentStatus.IDLE) }
    }
}
