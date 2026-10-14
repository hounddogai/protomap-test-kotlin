package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryFreightInvoiceAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryFreightInvoiceAdjustment("code-923", "label-420")
        assertEquals(PrimaryFreightInvoiceAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPrimaryFreightInvoiceAdjustment("code-923", "label-420")
        try {
            item.moveTo(PrimaryFreightInvoiceAdjustmentStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryFreightInvoiceAdjustmentStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryFreightInvoiceAdjustment("code-923", "label-420")
        item.status = PrimaryFreightInvoiceAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryFreightInvoiceAdjustmentStatus.REQUESTED) }
    }
}
