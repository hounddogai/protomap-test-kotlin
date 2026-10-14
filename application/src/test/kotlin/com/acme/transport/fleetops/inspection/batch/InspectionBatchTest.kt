package com.acme.transport.fleetops.inspection.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionBatchTest {
    @Test
    fun `starts in idle`() {
        val item = newInspectionBatch("code-336", "name-171")
        assertEquals(InspectionBatchStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newInspectionBatch("code-336", "name-171")
        try {
            item.moveTo(InspectionBatchStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionBatchStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newInspectionBatch("code-336", "name-171")
        item.status = InspectionBatchStatus.STOPPED
        assertFails("move") { item.moveTo(InspectionBatchStatus.IDLE) }
    }
}
