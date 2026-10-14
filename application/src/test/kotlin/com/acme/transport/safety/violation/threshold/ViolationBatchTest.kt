package com.acme.transport.safety.violation.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newViolationBatch("code-136", "label-610")
        assertEquals(ViolationBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newViolationBatch("code-136", "label-610")
        try {
            item.moveTo(ViolationBatchStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationBatchStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newViolationBatch("code-136", "label-610")
        item.status = ViolationBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(ViolationBatchStatus.REQUESTED) }
    }
}
