package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newFuelSurchargeEntry("code-772", "title-498")
        assertEquals(FuelSurchargeEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newFuelSurchargeEntry("code-772", "title-498")
        try {
            item.moveTo(FuelSurchargeEntryStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeEntryStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFuelSurchargeEntry("code-772", "title-498")
        item.status = FuelSurchargeEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(FuelSurchargeEntryStatus.PLANNED) }
    }
}
