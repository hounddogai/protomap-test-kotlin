package com.acme.transport.safety.violation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualViolationBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newManualViolationBatch("code-498", "label-620")
        assertEquals(ManualViolationBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newManualViolationBatch("code-498", "label-620")
        try {
            item.moveTo(ManualViolationBatchStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualViolationBatchStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualViolationBatch("code-498", "label-620")
        item.status = ManualViolationBatchStatus.CLOSED
        assertFails("move") { item.moveTo(ManualViolationBatchStatus.DRAFT) }
    }
}
