package com.acme.transport.customs.inspection.inspectionestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingInspectionAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingInspectionAllocation("code-697", "title-87")
        assertEquals(PendingInspectionAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPendingInspectionAllocation("code-697", "title-87")
        try {
            item.moveTo(PendingInspectionAllocationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingInspectionAllocationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingInspectionAllocation("code-697", "title-87")
        item.status = PendingInspectionAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(PendingInspectionAllocationStatus.IDLE) }
    }
}
