package com.acme.transport.fleetops.inspection.inspectionschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalInspectionBatchTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalInspectionBatch("code-888", "label-62")
        assertEquals(GlobalInspectionBatchStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalInspectionBatch("code-888", "label-62")
        try {
            item.moveTo(GlobalInspectionBatchStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalInspectionBatchStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalInspectionBatch("code-888", "label-62")
        item.status = GlobalInspectionBatchStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalInspectionBatchStatus.PROPOSED) }
    }
}
