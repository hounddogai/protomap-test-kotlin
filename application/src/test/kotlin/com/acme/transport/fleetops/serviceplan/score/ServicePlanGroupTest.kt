package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newServicePlanGroup("code-674", "name-282")
        assertEquals(ServicePlanGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newServicePlanGroup("code-674", "name-282")
        try {
            item.moveTo(ServicePlanGroupStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanGroupStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newServicePlanGroup("code-674", "name-282")
        item.status = ServicePlanGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(ServicePlanGroupStatus.PLANNED) }
    }
}
