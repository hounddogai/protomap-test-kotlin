package com.acme.transport.fleetops.serviceplan.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanCycleTest {
    @Test
    fun `starts in requested`() {
        val item = newServicePlanCycle("code-840", "title-804")
        assertEquals(ServicePlanCycleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newServicePlanCycle("code-840", "title-804")
        try {
            item.moveTo(ServicePlanCycleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanCycleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newServicePlanCycle("code-840", "title-804")
        item.status = ServicePlanCycleStatus.EXPIRED
        assertFails("move") { item.moveTo(ServicePlanCycleStatus.REQUESTED) }
    }
}
