package com.acme.transport.freightpay.fuelsurcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFuelSurchargeQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedFuelSurchargeQuota("code-766", "name-782")
        assertEquals(WeightedFuelSurchargeQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newWeightedFuelSurchargeQuota("code-766", "name-782")
        try {
            item.moveTo(WeightedFuelSurchargeQuotaStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFuelSurchargeQuotaStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedFuelSurchargeQuota("code-766", "name-782")
        item.status = WeightedFuelSurchargeQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedFuelSurchargeQuotaStatus.DRAFT) }
    }
}
