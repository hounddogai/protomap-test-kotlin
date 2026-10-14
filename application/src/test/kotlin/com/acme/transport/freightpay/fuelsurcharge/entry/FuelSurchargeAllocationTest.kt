package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelSurchargeAllocation("code-157", "title-345")
        assertEquals(FuelSurchargeAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newFuelSurchargeAllocation("code-157", "title-345")
        try {
            item.moveTo(FuelSurchargeAllocationStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeAllocationStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelSurchargeAllocation("code-157", "title-345")
        item.status = FuelSurchargeAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(FuelSurchargeAllocationStatus.IDLE) }
    }
}
