package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeAdjustmentTest {
    @Test
    fun `starts in new`() {
        val item = newFuelSurchargeAdjustment("code-423", "name-642")
        assertEquals(FuelSurchargeAdjustmentStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newFuelSurchargeAdjustment("code-423", "name-642")
        try {
            item.moveTo(FuelSurchargeAdjustmentStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeAdjustmentStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFuelSurchargeAdjustment("code-423", "name-642")
        item.status = FuelSurchargeAdjustmentStatus.FAILED
        assertFails("move") { item.moveTo(FuelSurchargeAdjustmentStatus.NEW) }
    }
}
