package com.acme.transport.fleetops.serviceplan.planquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanMilestoneTest {
    @Test
    fun `starts in idle`() {
        val item = newServicePlanMilestone("code-483", "title-800")
        assertEquals(ServicePlanMilestoneStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newServicePlanMilestone("code-483", "title-800")
        try {
            item.moveTo(ServicePlanMilestoneStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanMilestoneStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServicePlanMilestone("code-483", "title-800")
        item.status = ServicePlanMilestoneStatus.STOPPED
        assertFails("move") { item.moveTo(ServicePlanMilestoneStatus.IDLE) }
    }
}
