package com.acme.transport.freightpay.fuelsurcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FuelSurchargeBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newFuelSurchargeBaseline("code-843", "title-576")
        assertEquals(FuelSurchargeBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newFuelSurchargeBaseline("code-843", "title-576")
        try {
            item.moveTo(FuelSurchargeBaselineStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FuelSurchargeBaselineStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFuelSurchargeBaseline("code-843", "title-576")
        item.status = FuelSurchargeBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(FuelSurchargeBaselineStatus.OPEN) }
    }
}
