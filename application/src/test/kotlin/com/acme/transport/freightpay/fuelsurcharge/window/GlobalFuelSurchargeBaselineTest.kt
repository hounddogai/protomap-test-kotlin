package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalFuelSurchargeBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalFuelSurchargeBaseline("code-689", "title-449")
        assertEquals(GlobalFuelSurchargeBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newGlobalFuelSurchargeBaseline("code-689", "title-449")
        try {
            item.moveTo(GlobalFuelSurchargeBaselineStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalFuelSurchargeBaselineStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalFuelSurchargeBaseline("code-689", "title-449")
        item.status = GlobalFuelSurchargeBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalFuelSurchargeBaselineStatus.DRAFT) }
    }
}
