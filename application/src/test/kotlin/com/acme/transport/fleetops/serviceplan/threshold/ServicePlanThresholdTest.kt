package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newServicePlanThreshold("code-919", "title-373")
        assertEquals(ServicePlanThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newServicePlanThreshold("code-919", "title-373")
        try {
            item.moveTo(ServicePlanThresholdStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanThresholdStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newServicePlanThreshold("code-919", "title-373")
        item.status = ServicePlanThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(ServicePlanThresholdStatus.REQUESTED) }
    }
}
