package com.acme.transport.customs.declaration.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualDeclarationAdjustmentTest {
    @Test
    fun `starts in planned`() {
        val item = newManualDeclarationAdjustment("code-352", "label-518")
        assertEquals(ManualDeclarationAdjustmentStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newManualDeclarationAdjustment("code-352", "label-518")
        try {
            item.moveTo(ManualDeclarationAdjustmentStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualDeclarationAdjustmentStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualDeclarationAdjustment("code-352", "label-518")
        item.status = ManualDeclarationAdjustmentStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualDeclarationAdjustmentStatus.PLANNED) }
    }
}
