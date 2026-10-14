package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeScoreTest {
    @Test
    fun `starts in open`() {
        val item = newFuelSurchargeScore("code-423", "name-166")
        assertEquals(FuelSurchargeScoreStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newFuelSurchargeScore("code-423", "name-166")
        try {
            item.moveTo(FuelSurchargeScoreStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeScoreStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelSurchargeScore("code-423", "name-166")
        item.status = FuelSurchargeScoreStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelSurchargeScoreStatus.OPEN) }
    }
}
