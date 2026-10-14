package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAvailabilitySlotTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedAvailabilitySlot("code-952", "name-864")
        assertEquals(WeightedAvailabilitySlotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newWeightedAvailabilitySlot("code-952", "name-864")
        try {
            item.moveTo(WeightedAvailabilitySlotStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAvailabilitySlotStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedAvailabilitySlot("code-952", "name-864")
        item.status = WeightedAvailabilitySlotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedAvailabilitySlotStatus.PROPOSED) }
    }
}
