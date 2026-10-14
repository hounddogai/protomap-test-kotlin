package com.acme.transport.freightpay.fuelsurcharge.surchargewindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeTierTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelSurchargeTier("code-413", "label-379")
        assertEquals(FuelSurchargeTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFuelSurchargeTier("code-413", "label-379")
        try {
            item.moveTo(FuelSurchargeTierStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeTierStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelSurchargeTier("code-413", "label-379")
        item.status = FuelSurchargeTierStatus.STOPPED
        assertFails("move") { item.moveTo(FuelSurchargeTierStatus.IDLE) }
    }
}
