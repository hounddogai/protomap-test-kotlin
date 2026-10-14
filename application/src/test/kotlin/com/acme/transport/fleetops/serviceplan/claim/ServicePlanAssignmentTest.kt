package com.acme.transport.fleetops.serviceplan.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newServicePlanAssignment("code-122", "title-425")
        assertEquals(ServicePlanAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newServicePlanAssignment("code-122", "title-425")
        try {
            item.moveTo(ServicePlanAssignmentStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanAssignmentStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newServicePlanAssignment("code-122", "title-425")
        item.status = ServicePlanAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(ServicePlanAssignmentStatus.REQUESTED) }
    }
}
