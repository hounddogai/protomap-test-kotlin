package com.acme.transport.safety.violation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackViolationAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackViolationAllocation("code-357", "label-521")
        assertEquals(FallbackViolationAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackViolationAllocation("code-357", "label-521")
        try {
            item.moveTo(FallbackViolationAllocationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackViolationAllocationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackViolationAllocation("code-357", "label-521")
        item.status = FallbackViolationAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackViolationAllocationStatus.REQUESTED) }
    }
}
