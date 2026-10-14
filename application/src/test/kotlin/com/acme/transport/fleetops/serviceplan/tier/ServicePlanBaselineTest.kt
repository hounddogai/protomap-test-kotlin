package com.acme.transport.fleetops.serviceplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanBaselineTest {
    @Test
    fun `starts in created`() {
        val item = newServicePlanBaseline("code-451", "label-848")
        assertEquals(ServicePlanBaselineStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newServicePlanBaseline("code-451", "label-848")
        try {
            item.moveTo(ServicePlanBaselineStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanBaselineStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newServicePlanBaseline("code-451", "label-848")
        item.status = ServicePlanBaselineStatus.RETIRED
        assertFails("move") { item.moveTo(ServicePlanBaselineStatus.CREATED) }
    }
}
