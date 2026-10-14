package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultFuelSurchargeWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultFuelSurchargeWindow("code-532", "label-625")
        assertEquals(DefaultFuelSurchargeWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDefaultFuelSurchargeWindow("code-532", "label-625")
        try {
            item.moveTo(DefaultFuelSurchargeWindowStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultFuelSurchargeWindowStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultFuelSurchargeWindow("code-532", "label-625")
        item.status = DefaultFuelSurchargeWindowStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultFuelSurchargeWindowStatus.DRAFT) }
    }
}
