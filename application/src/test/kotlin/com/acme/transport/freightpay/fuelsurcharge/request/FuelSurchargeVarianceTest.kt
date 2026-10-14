package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newFuelSurchargeVariance("code-122", "label-610")
        assertEquals(FuelSurchargeVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newFuelSurchargeVariance("code-122", "label-610")
        try {
            item.moveTo(FuelSurchargeVarianceStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeVarianceStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFuelSurchargeVariance("code-122", "label-610")
        item.status = FuelSurchargeVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(FuelSurchargeVarianceStatus.PLANNED) }
    }
}
