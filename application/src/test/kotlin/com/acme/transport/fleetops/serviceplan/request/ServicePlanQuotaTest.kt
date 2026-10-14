package com.acme.transport.fleetops.serviceplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanQuotaTest {
    @Test
    fun `starts in planned`() {
        val item = newServicePlanQuota("code-548", "label-33")
        assertEquals(ServicePlanQuotaStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newServicePlanQuota("code-548", "label-33")
        try {
            item.moveTo(ServicePlanQuotaStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanQuotaStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newServicePlanQuota("code-548", "label-33")
        item.status = ServicePlanQuotaStatus.CANCELLED
        assertFails("move") { item.moveTo(ServicePlanQuotaStatus.PLANNED) }
    }
}
