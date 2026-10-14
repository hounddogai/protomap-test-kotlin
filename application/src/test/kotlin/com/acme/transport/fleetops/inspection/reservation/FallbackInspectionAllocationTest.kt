package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackInspectionAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackInspectionAllocation("code-770", "name-7")
        assertEquals(FallbackInspectionAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackInspectionAllocation("code-770", "name-7")
        try {
            item.moveTo(FallbackInspectionAllocationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackInspectionAllocationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackInspectionAllocation("code-770", "name-7")
        item.status = FallbackInspectionAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackInspectionAllocationStatus.REQUESTED) }
    }
}
