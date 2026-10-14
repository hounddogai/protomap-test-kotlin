package com.acme.transport.freightpay.fuelsurcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredFuelSurchargeAdjustmentTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredFuelSurchargeAdjustment("code-913", "name-420")
        assertEquals(TieredFuelSurchargeAdjustmentStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredFuelSurchargeAdjustment("code-913", "name-420")
        try {
            item.moveTo(TieredFuelSurchargeAdjustmentStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredFuelSurchargeAdjustmentStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredFuelSurchargeAdjustment("code-913", "name-420")
        item.status = TieredFuelSurchargeAdjustmentStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredFuelSurchargeAdjustmentStatus.PROPOSED) }
    }
}
