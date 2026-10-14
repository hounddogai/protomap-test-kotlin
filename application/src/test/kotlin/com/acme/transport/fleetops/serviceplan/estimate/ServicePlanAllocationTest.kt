package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newServicePlanAllocation("code-131", "label-869")
        assertEquals(ServicePlanAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newServicePlanAllocation("code-131", "label-869")
        try {
            item.moveTo(ServicePlanAllocationStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanAllocationStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newServicePlanAllocation("code-131", "label-869")
        item.status = ServicePlanAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(ServicePlanAllocationStatus.PLANNED) }
    }
}
