package com.acme.transport.freightpay.fuelsurcharge.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeSlotTest {
    @Test
    fun `starts in created`() {
        val item = newFuelSurchargeSlot("code-745", "title-127")
        assertEquals(FuelSurchargeSlotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFuelSurchargeSlot("code-745", "title-127")
        try {
            item.moveTo(FuelSurchargeSlotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeSlotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFuelSurchargeSlot("code-745", "title-127")
        item.status = FuelSurchargeSlotStatus.RETIRED
        assertFails("move") { item.moveTo(FuelSurchargeSlotStatus.CREATED) }
    }
}
