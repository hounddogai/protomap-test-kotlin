package com.acme.transport.fleetops.serviceplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanTargetTest {
    @Test
    fun `starts in open`() {
        val item = newServicePlanTarget("code-302", "label-365")
        assertEquals(ServicePlanTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newServicePlanTarget("code-302", "label-365")
        try {
            item.moveTo(ServicePlanTargetStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanTargetStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newServicePlanTarget("code-302", "label-365")
        item.status = ServicePlanTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(ServicePlanTargetStatus.OPEN) }
    }
}
