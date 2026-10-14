package com.acme.transport.freightpay.freightinvoice.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceDigestTest {
    @Test
    fun `starts in planned`() {
        val item = newFreightInvoiceDigest("code-663", "label-487")
        assertEquals(FreightInvoiceDigestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFreightInvoiceDigest("code-663", "label-487")
        try {
            item.moveTo(FreightInvoiceDigestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceDigestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFreightInvoiceDigest("code-663", "label-487")
        item.status = FreightInvoiceDigestStatus.CANCELLED
        assertFails("move") { item.moveTo(FreightInvoiceDigestStatus.PLANNED) }
    }
}
