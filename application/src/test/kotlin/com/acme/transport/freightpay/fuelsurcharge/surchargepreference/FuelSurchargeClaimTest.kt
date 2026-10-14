package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeClaimTest {
    @Test
    fun `starts in new`() {
        val item = newFuelSurchargeClaim("code-747", "title-616")
        assertEquals(FuelSurchargeClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newFuelSurchargeClaim("code-747", "title-616")
        try {
            item.moveTo(FuelSurchargeClaimStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeClaimStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFuelSurchargeClaim("code-747", "title-616")
        item.status = FuelSurchargeClaimStatus.FAILED
        assertFails("move") { item.moveTo(FuelSurchargeClaimStatus.NEW) }
    }
}
