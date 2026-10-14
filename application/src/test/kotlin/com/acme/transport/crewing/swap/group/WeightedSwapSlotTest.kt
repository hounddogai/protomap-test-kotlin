package com.acme.transport.crewing.swap.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedSwapSlotTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedSwapSlot("code-197", "name-482")
        assertEquals(WeightedSwapSlotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newWeightedSwapSlot("code-197", "name-482")
        try {
            item.moveTo(WeightedSwapSlotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedSwapSlotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedSwapSlot("code-197", "name-482")
        item.status = WeightedSwapSlotStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedSwapSlotStatus.CREATED) }
    }
}
