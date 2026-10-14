package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultFuelSurchargeRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultFuelSurchargeRule("code-690", "label-870")
        assertEquals(DefaultFuelSurchargeRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultFuelSurchargeRule("code-690", "label-870")
        try {
            item.moveTo(DefaultFuelSurchargeRuleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultFuelSurchargeRuleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultFuelSurchargeRule("code-690", "label-870")
        item.status = DefaultFuelSurchargeRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultFuelSurchargeRuleStatus.REQUESTED) }
    }
}
