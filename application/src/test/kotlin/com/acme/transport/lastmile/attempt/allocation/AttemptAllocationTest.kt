package com.acme.transport.lastmile.attempt.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptAllocationTest {
    @Test
    fun `starts in open`() {
        val item = newAttemptAllocation("code-62", "title-412")
        assertEquals(AttemptAllocationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAttemptAllocation("code-62", "title-412")
        try {
            item.moveTo(AttemptAllocationStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptAllocationStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAttemptAllocation("code-62", "title-412")
        item.status = AttemptAllocationStatus.ARCHIVED
        assertFails("move") { item.moveTo(AttemptAllocationStatus.OPEN) }
    }
}
