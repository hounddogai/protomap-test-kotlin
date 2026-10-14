package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFreightInvoicePolicyTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedFreightInvoicePolicy("code-983", "label-29")
        assertEquals(WeightedFreightInvoicePolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newWeightedFreightInvoicePolicy("code-983", "label-29")
        try {
            item.moveTo(WeightedFreightInvoicePolicyStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFreightInvoicePolicyStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedFreightInvoicePolicy("code-983", "label-29")
        item.status = WeightedFreightInvoicePolicyStatus.FAILED
        assertFails("move") { item.moveTo(WeightedFreightInvoicePolicyStatus.NEW) }
    }
}
