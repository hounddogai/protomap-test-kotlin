package com.acme.transport.safety.violation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedViolationPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedViolationPolicy("code-392", "label-979")
        assertEquals(BatchedViolationPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newBatchedViolationPolicy("code-392", "label-979")
        try {
            item.moveTo(BatchedViolationPolicyStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedViolationPolicyStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedViolationPolicy("code-392", "label-979")
        item.status = BatchedViolationPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedViolationPolicyStatus.OPEN) }
    }
}
