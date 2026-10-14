package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTenderAllocationTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedTenderAllocation("code-729", "label-738")
        assertEquals(WeightedTenderAllocationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedTenderAllocation("code-729", "label-738")
        try {
            item.moveTo(WeightedTenderAllocationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTenderAllocationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedTenderAllocation("code-729", "label-738")
        item.status = WeightedTenderAllocationStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedTenderAllocationStatus.REQUESTED) }
    }
}
