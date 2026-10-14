package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalFuelSurchargeAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalFuelSurchargeAdjustment("code-882", "title-446")
        assertEquals(SeasonalFuelSurchargeAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newSeasonalFuelSurchargeAdjustment("code-882", "title-446")
        try {
            item.moveTo(SeasonalFuelSurchargeAdjustmentStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalFuelSurchargeAdjustmentStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalFuelSurchargeAdjustment("code-882", "title-446")
        item.status = SeasonalFuelSurchargeAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalFuelSurchargeAdjustmentStatus.PROPOSED) }
    }
}
