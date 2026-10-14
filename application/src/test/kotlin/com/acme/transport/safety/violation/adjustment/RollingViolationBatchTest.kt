package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingViolationBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingViolationBatch("code-767", "label-260")
        assertEquals(RollingViolationBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingViolationBatch("code-767", "label-260")
        try {
            item.moveTo(RollingViolationBatchStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingViolationBatchStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingViolationBatch("code-767", "label-260")
        item.status = RollingViolationBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingViolationBatchStatus.PROPOSED) }
    }
}
