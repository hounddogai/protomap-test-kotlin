package com.acme.transport.freightpay.fuelsurcharge.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newFuelSurchargeException("code-559", "title-912")
        assertEquals(FuelSurchargeExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFuelSurchargeException("code-559", "title-912")
        try {
            item.moveTo(FuelSurchargeExceptionStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeExceptionStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFuelSurchargeException("code-559", "title-912")
        item.status = FuelSurchargeExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(FuelSurchargeExceptionStatus.IDLE) }
    }
}
