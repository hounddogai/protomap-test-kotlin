package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newServicePlanPolicy("code-135", "label-173")
        assertEquals(ServicePlanPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newServicePlanPolicy("code-135", "label-173")
        try {
            item.moveTo(ServicePlanPolicyStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanPolicyStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newServicePlanPolicy("code-135", "label-173")
        item.status = ServicePlanPolicyStatus.FAILED
        assertFails("move") { item.moveTo(ServicePlanPolicyStatus.NEW) }
    }
}
