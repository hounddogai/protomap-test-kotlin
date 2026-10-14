package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackFuelSurchargeQuotaTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackFuelSurchargeQuota("code-245", "name-603")
        assertEquals(FallbackFuelSurchargeQuotaStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackFuelSurchargeQuota("code-245", "name-603")
        try {
            item.moveTo(FallbackFuelSurchargeQuotaStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackFuelSurchargeQuotaStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackFuelSurchargeQuota("code-245", "name-603")
        item.status = FallbackFuelSurchargeQuotaStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackFuelSurchargeQuotaStatus.DRAFT) }
    }
}
