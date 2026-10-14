package com.acme.transport.freightpay.freightinvoice.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedFreightInvoicePlanTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedFreightInvoicePlan("code-277", "label-813")
        assertEquals(CappedFreightInvoicePlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newCappedFreightInvoicePlan("code-277", "label-813")
        try {
            item.moveTo(CappedFreightInvoicePlanStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedFreightInvoicePlanStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedFreightInvoicePlan("code-277", "label-813")
        item.status = CappedFreightInvoicePlanStatus.CLOSED
        assertFails("move") { item.moveTo(CappedFreightInvoicePlanStatus.DRAFT) }
    }
}
