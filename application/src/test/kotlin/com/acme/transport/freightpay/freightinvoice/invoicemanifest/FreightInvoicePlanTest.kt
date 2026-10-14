package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoicePlanTest {
    @Test
    fun `starts in idle`() {
        val item = newFreightInvoicePlan("code-283", "label-968")
        assertEquals(FreightInvoicePlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFreightInvoicePlan("code-283", "label-968")
        try {
            item.moveTo(FreightInvoicePlanStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoicePlanStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFreightInvoicePlan("code-283", "label-968")
        item.status = FreightInvoicePlanStatus.STOPPED
        assertFails("move") { item.moveTo(FreightInvoicePlanStatus.IDLE) }
    }
}
