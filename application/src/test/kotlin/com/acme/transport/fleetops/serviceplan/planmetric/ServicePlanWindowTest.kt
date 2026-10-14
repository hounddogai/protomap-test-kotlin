package com.acme.transport.fleetops.serviceplan.planmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newServicePlanWindow("code-965", "name-755")
        assertEquals(ServicePlanWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newServicePlanWindow("code-965", "name-755")
        try {
            item.moveTo(ServicePlanWindowStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanWindowStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newServicePlanWindow("code-965", "name-755")
        item.status = ServicePlanWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ServicePlanWindowStatus.PROPOSED) }
    }
}
