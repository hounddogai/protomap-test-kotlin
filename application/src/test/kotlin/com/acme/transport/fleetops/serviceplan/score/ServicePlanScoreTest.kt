package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newServicePlanScore("code-874", "name-49")
        assertEquals(ServicePlanScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newServicePlanScore("code-874", "name-49")
        try {
            item.moveTo(ServicePlanScoreStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanScoreStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServicePlanScore("code-874", "name-49")
        item.status = ServicePlanScoreStatus.STOPPED
        assertFails("move") { item.moveTo(ServicePlanScoreStatus.IDLE) }
    }
}
