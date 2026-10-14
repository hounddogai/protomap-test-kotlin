package com.acme.transport.freightpay.fuelsurcharge.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeSegmentTest {
    @Test
    fun `starts in draft`() {
        val item = newFuelSurchargeSegment("code-740", "name-670")
        assertEquals(FuelSurchargeSegmentStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFuelSurchargeSegment("code-740", "name-670")
        try {
            item.moveTo(FuelSurchargeSegmentStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeSegmentStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFuelSurchargeSegment("code-740", "name-670")
        item.status = FuelSurchargeSegmentStatus.CLOSED
        assertFails("move") { item.moveTo(FuelSurchargeSegmentStatus.DRAFT) }
    }
}
