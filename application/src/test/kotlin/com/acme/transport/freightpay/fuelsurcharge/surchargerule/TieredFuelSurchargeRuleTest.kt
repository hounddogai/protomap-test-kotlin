package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredFuelSurchargeRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredFuelSurchargeRule("code-715", "title-410")
        assertEquals(TieredFuelSurchargeRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTieredFuelSurchargeRule("code-715", "title-410")
        try {
            item.moveTo(TieredFuelSurchargeRuleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredFuelSurchargeRuleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredFuelSurchargeRule("code-715", "title-410")
        item.status = TieredFuelSurchargeRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredFuelSurchargeRuleStatus.REQUESTED) }
    }
}
