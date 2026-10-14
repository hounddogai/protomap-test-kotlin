package com.acme.transport.emissions.routeoffset.offsetbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRouteOffsetSlotTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedRouteOffsetSlot("code-195", "label-853")
        assertEquals(WeightedRouteOffsetSlotStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newWeightedRouteOffsetSlot("code-195", "label-853")
        try {
            item.moveTo(WeightedRouteOffsetSlotStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRouteOffsetSlotStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedRouteOffsetSlot("code-195", "label-853")
        item.status = WeightedRouteOffsetSlotStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedRouteOffsetSlotStatus.OPEN) }
    }
}
