package com.acme.transport.fleetops.serviceplan.planrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanRecordTest {
    @Test
    fun `starts in created`() {
        val item = newServicePlanRecord("code-84", "name-314")
        assertEquals(ServicePlanRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newServicePlanRecord("code-84", "name-314")
        try {
            item.moveTo(ServicePlanRecordStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanRecordStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newServicePlanRecord("code-84", "name-314")
        item.status = ServicePlanRecordStatus.RETIRED
        assertFails("move") { item.moveTo(ServicePlanRecordStatus.CREATED) }
    }
}
