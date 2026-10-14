package com.acme.transport.freightpay.freightinvoice.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceBundleTest {
    @Test
    fun `starts in new`() {
        val item = newFreightInvoiceBundle("code-536", "name-111")
        assertEquals(FreightInvoiceBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newFreightInvoiceBundle("code-536", "name-111")
        try {
            item.moveTo(FreightInvoiceBundleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceBundleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFreightInvoiceBundle("code-536", "name-111")
        item.status = FreightInvoiceBundleStatus.FAILED
        assertFails("move") { item.moveTo(FreightInvoiceBundleStatus.NEW) }
    }
}
