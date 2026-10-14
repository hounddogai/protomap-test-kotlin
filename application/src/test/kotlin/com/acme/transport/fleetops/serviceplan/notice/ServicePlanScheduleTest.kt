package com.acme.transport.fleetops.serviceplan.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanScheduleTest {
    @Test
    fun `starts in idle`() {
        val item = newServicePlanSchedule("code-492", "name-418")
        assertEquals(ServicePlanScheduleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newServicePlanSchedule("code-492", "name-418")
        try {
            item.moveTo(ServicePlanScheduleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanScheduleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServicePlanSchedule("code-492", "name-418")
        item.status = ServicePlanScheduleStatus.STOPPED
        assertFails("move") { item.moveTo(ServicePlanScheduleStatus.IDLE) }
    }
}
