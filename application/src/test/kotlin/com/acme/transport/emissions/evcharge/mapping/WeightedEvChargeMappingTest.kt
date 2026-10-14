package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedEvChargeMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedEvChargeMapping("code-85", "name-209")
        assertEquals(WeightedEvChargeMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newWeightedEvChargeMapping("code-85", "name-209")
        try {
            item.moveTo(WeightedEvChargeMappingStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedEvChargeMappingStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedEvChargeMapping("code-85", "name-209")
        item.status = WeightedEvChargeMappingStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedEvChargeMappingStatus.IDLE) }
    }
}
