package com.acme.transport.freightpay.fuelsurcharge.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeTargetTest {
    @Test
    fun `starts in draft`() {
        val item = newFuelSurchargeTarget("code-31", "name-727")
        assertEquals(FuelSurchargeTargetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newFuelSurchargeTarget("code-31", "name-727")
        try {
            item.moveTo(FuelSurchargeTargetStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeTargetStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFuelSurchargeTarget("code-31", "name-727")
        item.status = FuelSurchargeTargetStatus.CLOSED
        assertFails("move") { item.moveTo(FuelSurchargeTargetStatus.DRAFT) }
    }
}
