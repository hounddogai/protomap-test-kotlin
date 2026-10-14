package com.acme.transport.freightpay.accessorial.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAccessorialAllocationTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedAccessorialAllocation("code-785", "label-593")
        assertEquals(WeightedAccessorialAllocationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newWeightedAccessorialAllocation("code-785", "label-593")
        try {
            item.moveTo(WeightedAccessorialAllocationStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAccessorialAllocationStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedAccessorialAllocation("code-785", "label-593")
        item.status = WeightedAccessorialAllocationStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedAccessorialAllocationStatus.PLANNED) }
    }
}
