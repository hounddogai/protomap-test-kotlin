package com.acme.transport.customs.inspection.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionAllocationTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionAllocation("code-725", "name-617")
        assertEquals(InspectionAllocationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newInspectionAllocation("code-725", "name-617")
        try {
            item.moveTo(InspectionAllocationStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionAllocationStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionAllocation("code-725", "name-617")
        item.status = InspectionAllocationStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionAllocationStatus.DRAFT) }
    }
}
