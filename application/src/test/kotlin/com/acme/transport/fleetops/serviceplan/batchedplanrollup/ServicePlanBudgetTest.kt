package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanBudgetTest {
    @Test
    fun `starts in idle`() {
        val item = newServicePlanBudget("code-779", "label-852")
        assertEquals(ServicePlanBudgetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newServicePlanBudget("code-779", "label-852")
        try {
            item.moveTo(ServicePlanBudgetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanBudgetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServicePlanBudget("code-779", "label-852")
        item.status = ServicePlanBudgetStatus.STOPPED
        assertFails("move") { item.moveTo(ServicePlanBudgetStatus.IDLE) }
    }
}
