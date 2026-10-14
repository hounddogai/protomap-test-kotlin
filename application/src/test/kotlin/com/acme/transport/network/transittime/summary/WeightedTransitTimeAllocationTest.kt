package com.acme.transport.network.transittime.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTransitTimeAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedTransitTimeAllocation("code-42", "name-574")
        assertEquals(WeightedTransitTimeAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedTransitTimeAllocation("code-42", "name-574")
        try {
            item.moveTo(WeightedTransitTimeAllocationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTransitTimeAllocationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedTransitTimeAllocation("code-42", "name-574")
        item.status = WeightedTransitTimeAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedTransitTimeAllocationStatus.REQUESTED) }
    }
}
