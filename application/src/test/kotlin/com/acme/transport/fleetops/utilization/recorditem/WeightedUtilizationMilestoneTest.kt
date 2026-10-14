package com.acme.transport.fleetops.utilization.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedUtilizationMilestoneTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedUtilizationMilestone("code-510", "title-106")
        assertEquals(WeightedUtilizationMilestoneStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newWeightedUtilizationMilestone("code-510", "title-106")
        try {
            item.moveTo(WeightedUtilizationMilestoneStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedUtilizationMilestoneStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedUtilizationMilestone("code-510", "title-106")
        item.status = WeightedUtilizationMilestoneStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedUtilizationMilestoneStatus.DRAFT) }
    }
}
