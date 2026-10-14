package com.acme.transport.fleetops.inspection.inspectionschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedInspectionSlotTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedInspectionSlot("code-927", "title-705")
        assertEquals(BatchedInspectionSlotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedInspectionSlot("code-927", "title-705")
        try {
            item.moveTo(BatchedInspectionSlotStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedInspectionSlotStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedInspectionSlot("code-927", "title-705")
        item.status = BatchedInspectionSlotStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedInspectionSlotStatus.REQUESTED) }
    }
}
