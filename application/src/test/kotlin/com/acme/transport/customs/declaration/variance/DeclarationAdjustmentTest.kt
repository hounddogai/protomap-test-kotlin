package com.acme.transport.customs.declaration.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationAdjustmentTest {
    @Test
    fun `starts in planned`() {
        val item = newDeclarationAdjustment("code-561", "label-734")
        assertEquals(DeclarationAdjustmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDeclarationAdjustment("code-561", "label-734")
        try {
            item.moveTo(DeclarationAdjustmentStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationAdjustmentStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDeclarationAdjustment("code-561", "label-734")
        item.status = DeclarationAdjustmentStatus.CANCELLED
        assertFails("move") { item.moveTo(DeclarationAdjustmentStatus.PLANNED) }
    }
}
