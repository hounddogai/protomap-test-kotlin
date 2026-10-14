package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAvailabilityAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalAvailabilityAllocation("code-900", "name-278")
        assertEquals(GlobalAvailabilityAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newGlobalAvailabilityAllocation("code-900", "name-278")
        try {
            item.moveTo(GlobalAvailabilityAllocationStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAvailabilityAllocationStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalAvailabilityAllocation("code-900", "name-278")
        item.status = GlobalAvailabilityAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalAvailabilityAllocationStatus.PLANNED) }
    }
}
