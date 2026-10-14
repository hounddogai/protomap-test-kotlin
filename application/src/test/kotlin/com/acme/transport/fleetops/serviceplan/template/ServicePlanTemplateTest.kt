package com.acme.transport.fleetops.serviceplan.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newServicePlanTemplate("code-195", "name-556")
        assertEquals(ServicePlanTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newServicePlanTemplate("code-195", "name-556")
        try {
            item.moveTo(ServicePlanTemplateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanTemplateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newServicePlanTemplate("code-195", "name-556")
        item.status = ServicePlanTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(ServicePlanTemplateStatus.CREATED) }
    }
}
