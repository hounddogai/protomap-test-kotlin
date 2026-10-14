package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedServicePlanMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedServicePlanMilestone("code-838", "label-22")
        assertEquals(WeightedServicePlanMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newWeightedServicePlanMilestone("code-838", "label-22")
        try {
            item.moveTo(WeightedServicePlanMilestoneStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedServicePlanMilestoneStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedServicePlanMilestone("code-838", "label-22")
        item.status = WeightedServicePlanMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedServicePlanMilestoneStatus.REQUESTED) }
    }
}
