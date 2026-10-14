package com.acme.transport.fleetops.tire.tiremanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTireMilestoneTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedTireMilestone("code-251", "name-658")
        assertEquals(WeightedTireMilestoneStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedTireMilestone("code-251", "name-658")
        try {
            item.moveTo(WeightedTireMilestoneStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTireMilestoneStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedTireMilestone("code-251", "name-658")
        item.status = WeightedTireMilestoneStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedTireMilestoneStatus.PLANNED) }
    }
}
