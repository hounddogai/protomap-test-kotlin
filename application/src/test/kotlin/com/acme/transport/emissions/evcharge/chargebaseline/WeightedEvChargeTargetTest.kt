package com.acme.transport.emissions.evcharge.chargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedEvChargeTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedEvChargeTarget("code-85", "name-679")
        assertEquals(WeightedEvChargeTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedEvChargeTarget("code-85", "name-679")
        try {
            item.moveTo(WeightedEvChargeTargetStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedEvChargeTargetStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedEvChargeTarget("code-85", "name-679")
        item.status = WeightedEvChargeTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedEvChargeTargetStatus.PLANNED) }
    }
}
