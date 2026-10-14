package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDeclarationEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newPendingDeclarationEstimate("code-789", "label-67")
        assertEquals(PendingDeclarationEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newPendingDeclarationEstimate("code-789", "label-67")
        try {
            item.moveTo(PendingDeclarationEstimateStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDeclarationEstimateStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPendingDeclarationEstimate("code-789", "label-67")
        item.status = PendingDeclarationEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(PendingDeclarationEstimateStatus.OPEN) }
    }
}
