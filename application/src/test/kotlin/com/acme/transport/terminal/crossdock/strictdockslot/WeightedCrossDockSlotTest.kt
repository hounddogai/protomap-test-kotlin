package com.acme.transport.terminal.crossdock.strictdockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCrossDockSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedCrossDockSlot("code-854", "name-938")
        assertEquals(WeightedCrossDockSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedCrossDockSlot("code-854", "name-938")
        try {
            item.moveTo(WeightedCrossDockSlotStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCrossDockSlotStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedCrossDockSlot("code-854", "name-938")
        item.status = WeightedCrossDockSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedCrossDockSlotStatus.PLANNED) }
    }
}
