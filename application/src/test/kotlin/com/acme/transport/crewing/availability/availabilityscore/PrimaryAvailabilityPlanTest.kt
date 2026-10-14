package com.acme.transport.crewing.availability.availabilityscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryAvailabilityPlanTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryAvailabilityPlan("code-614", "name-238")
        assertEquals(PrimaryAvailabilityPlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPrimaryAvailabilityPlan("code-614", "name-238")
        try {
            item.moveTo(PrimaryAvailabilityPlanStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryAvailabilityPlanStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryAvailabilityPlan("code-614", "name-238")
        item.status = PrimaryAvailabilityPlanStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryAvailabilityPlanStatus.PLANNED) }
    }
}
