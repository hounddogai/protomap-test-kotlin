package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCapacityAllocationTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledCapacityAllocation("code-709", "label-495")
        assertEquals(ScheduledCapacityAllocationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledCapacityAllocation("code-709", "label-495")
        try {
            item.moveTo(ScheduledCapacityAllocationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCapacityAllocationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledCapacityAllocation("code-709", "label-495")
        item.status = ScheduledCapacityAllocationStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledCapacityAllocationStatus.CREATED) }
    }
}
