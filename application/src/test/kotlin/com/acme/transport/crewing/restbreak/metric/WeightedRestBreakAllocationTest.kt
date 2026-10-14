package com.acme.transport.crewing.restbreak.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRestBreakAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedRestBreakAllocation("code-43", "title-895")
        assertEquals(WeightedRestBreakAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedRestBreakAllocation("code-43", "title-895")
        try {
            item.moveTo(WeightedRestBreakAllocationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRestBreakAllocationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedRestBreakAllocation("code-43", "title-895")
        item.status = WeightedRestBreakAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedRestBreakAllocationStatus.REQUESTED) }
    }
}
