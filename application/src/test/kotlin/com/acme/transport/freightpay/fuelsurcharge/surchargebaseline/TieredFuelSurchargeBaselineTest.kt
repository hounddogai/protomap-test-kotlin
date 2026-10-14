package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredFuelSurchargeBaselineTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredFuelSurchargeBaseline("code-230", "label-561")
        assertEquals(TieredFuelSurchargeBaselineStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newTieredFuelSurchargeBaseline("code-230", "label-561")
        try {
            item.moveTo(TieredFuelSurchargeBaselineStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredFuelSurchargeBaselineStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredFuelSurchargeBaseline("code-230", "label-561")
        item.status = TieredFuelSurchargeBaselineStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredFuelSurchargeBaselineStatus.PROPOSED) }
    }
}
