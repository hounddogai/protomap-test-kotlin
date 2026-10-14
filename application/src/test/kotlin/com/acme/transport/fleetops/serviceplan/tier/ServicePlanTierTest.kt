package com.acme.transport.fleetops.serviceplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newServicePlanTier("code-169", "name-669")
        assertEquals(ServicePlanTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newServicePlanTier("code-169", "name-669")
        try {
            item.moveTo(ServicePlanTierStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanTierStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newServicePlanTier("code-169", "name-669")
        item.status = ServicePlanTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ServicePlanTierStatus.PROPOSED) }
    }
}
