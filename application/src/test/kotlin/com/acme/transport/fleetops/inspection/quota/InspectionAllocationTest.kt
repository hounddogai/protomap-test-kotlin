package com.acme.transport.fleetops.inspection.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionAllocation("code-872", "label-884")
        assertEquals(InspectionAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newInspectionAllocation("code-872", "label-884")
        try {
            item.moveTo(InspectionAllocationStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionAllocationStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionAllocation("code-872", "label-884")
        item.status = InspectionAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionAllocationStatus.DRAFT) }
    }
}
