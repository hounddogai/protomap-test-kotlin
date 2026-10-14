package com.acme.transport.fleetops.inspection.inspectionbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingInspectionSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingInspectionSlot("code-140", "name-738")
        assertEquals(PendingInspectionSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newPendingInspectionSlot("code-140", "name-738")
        try {
            item.moveTo(PendingInspectionSlotStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingInspectionSlotStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingInspectionSlot("code-140", "name-738")
        item.status = PendingInspectionSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingInspectionSlotStatus.PLANNED) }
    }
}
