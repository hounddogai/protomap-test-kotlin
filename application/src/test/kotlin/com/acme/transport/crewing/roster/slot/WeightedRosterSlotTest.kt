package com.acme.transport.crewing.roster.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRosterSlotTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedRosterSlot("code-155", "title-826")
        assertEquals(WeightedRosterSlotStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newWeightedRosterSlot("code-155", "title-826")
        try {
            item.moveTo(WeightedRosterSlotStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRosterSlotStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedRosterSlot("code-155", "title-826")
        item.status = WeightedRosterSlotStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedRosterSlotStatus.PLANNED) }
    }
}
