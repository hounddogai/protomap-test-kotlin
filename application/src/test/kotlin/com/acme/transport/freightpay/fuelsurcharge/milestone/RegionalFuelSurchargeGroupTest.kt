package com.acme.transport.freightpay.fuelsurcharge.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalFuelSurchargeGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalFuelSurchargeGroup("code-844", "label-229")
        assertEquals(RegionalFuelSurchargeGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRegionalFuelSurchargeGroup("code-844", "label-229")
        try {
            item.moveTo(RegionalFuelSurchargeGroupStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalFuelSurchargeGroupStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalFuelSurchargeGroup("code-844", "label-229")
        item.status = RegionalFuelSurchargeGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalFuelSurchargeGroupStatus.PLANNED) }
    }
}
