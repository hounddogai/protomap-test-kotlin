package com.acme.transport.fleetops.serviceplan.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledServicePlanTierTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledServicePlanTier("code-520", "title-174")
        assertEquals(ScheduledServicePlanTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newScheduledServicePlanTier("code-520", "title-174")
        try {
            item.moveTo(ScheduledServicePlanTierStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledServicePlanTierStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledServicePlanTier("code-520", "title-174")
        item.status = ScheduledServicePlanTierStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledServicePlanTierStatus.PLANNED) }
    }
}
