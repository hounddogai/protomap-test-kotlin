package com.acme.transport.safety.violation.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalViolationBatchTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalViolationBatch("code-220", "name-396")
        assertEquals(GlobalViolationBatchStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newGlobalViolationBatch("code-220", "name-396")
        try {
            item.moveTo(GlobalViolationBatchStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalViolationBatchStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalViolationBatch("code-220", "name-396")
        item.status = GlobalViolationBatchStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalViolationBatchStatus.IDLE) }
    }
}
