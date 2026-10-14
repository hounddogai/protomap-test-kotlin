package com.acme.transport.customs.clearance.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedClearanceEstimateTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedClearanceEstimate("code-354", "name-290")
        assertEquals(WeightedClearanceEstimateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newWeightedClearanceEstimate("code-354", "name-290")
        try {
            item.moveTo(WeightedClearanceEstimateStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedClearanceEstimateStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedClearanceEstimate("code-354", "name-290")
        item.status = WeightedClearanceEstimateStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedClearanceEstimateStatus.OPEN) }
    }
}
