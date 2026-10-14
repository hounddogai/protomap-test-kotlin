package com.acme.transport.crewing.roster.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedRosterAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newBatchedRosterAdjustment("code-829", "label-893")
        assertEquals(BatchedRosterAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newBatchedRosterAdjustment("code-829", "label-893")
        try {
            item.moveTo(BatchedRosterAdjustmentStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedRosterAdjustmentStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBatchedRosterAdjustment("code-829", "label-893")
        item.status = BatchedRosterAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(BatchedRosterAdjustmentStatus.IDLE) }
    }
}
