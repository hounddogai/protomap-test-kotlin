package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeRequestTest {
    @Test
    fun `starts in requested`() {
        val item = newFuelSurchargeRequest("code-65", "label-15")
        assertEquals(FuelSurchargeRequestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newFuelSurchargeRequest("code-65", "label-15")
        try {
            item.moveTo(FuelSurchargeRequestStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeRequestStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFuelSurchargeRequest("code-65", "label-15")
        item.status = FuelSurchargeRequestStatus.EXPIRED
        assertFails("move") { item.moveTo(FuelSurchargeRequestStatus.REQUESTED) }
    }
}
