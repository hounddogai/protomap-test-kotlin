package com.acme.transport.customs.declaration.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDeclarationAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredDeclarationAssignment("code-189", "label-887")
        assertEquals(TieredDeclarationAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTieredDeclarationAssignment("code-189", "label-887")
        try {
            item.moveTo(TieredDeclarationAssignmentStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDeclarationAssignmentStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredDeclarationAssignment("code-189", "label-887")
        item.status = TieredDeclarationAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(TieredDeclarationAssignmentStatus.IDLE) }
    }
}
