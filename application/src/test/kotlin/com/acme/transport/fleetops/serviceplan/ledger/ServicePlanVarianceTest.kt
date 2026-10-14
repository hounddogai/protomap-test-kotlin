package com.acme.transport.fleetops.serviceplan.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newServicePlanVariance("code-947", "label-975")
        assertEquals(ServicePlanVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newServicePlanVariance("code-947", "label-975")
        try {
            item.moveTo(ServicePlanVarianceStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanVarianceStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newServicePlanVariance("code-947", "label-975")
        item.status = ServicePlanVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ServicePlanVarianceStatus.PROPOSED) }
    }
}
