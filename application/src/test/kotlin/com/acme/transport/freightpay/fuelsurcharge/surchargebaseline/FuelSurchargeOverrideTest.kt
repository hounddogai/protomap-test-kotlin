package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newFuelSurchargeOverride("code-483", "title-741")
        assertEquals(FuelSurchargeOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newFuelSurchargeOverride("code-483", "title-741")
        try {
            item.moveTo(FuelSurchargeOverrideStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeOverrideStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFuelSurchargeOverride("code-483", "title-741")
        item.status = FuelSurchargeOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(FuelSurchargeOverrideStatus.DRAFT) }
    }
}
