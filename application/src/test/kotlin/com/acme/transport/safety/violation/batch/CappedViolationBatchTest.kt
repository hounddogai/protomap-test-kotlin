package com.acme.transport.safety.violation.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedViolationBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedViolationBatch("code-565", "label-529")
        assertEquals(CappedViolationBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedViolationBatch("code-565", "label-529")
        try {
            item.moveTo(CappedViolationBatchStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedViolationBatchStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedViolationBatch("code-565", "label-529")
        item.status = CappedViolationBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedViolationBatchStatus.PROPOSED) }
    }
}
