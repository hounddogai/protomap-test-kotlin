package com.acme.transport.freightpay.freightinvoice.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newFreightInvoiceQuota("code-961", "label-848")
        assertEquals(FreightInvoiceQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFreightInvoiceQuota("code-961", "label-848")
        try {
            item.moveTo(FreightInvoiceQuotaStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceQuotaStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFreightInvoiceQuota("code-961", "label-848")
        item.status = FreightInvoiceQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(FreightInvoiceQuotaStatus.REQUESTED) }
    }
}
