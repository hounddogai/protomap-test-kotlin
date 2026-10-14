package com.acme.transport.freightpay.freightinvoice.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFreightInvoiceTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientFreightInvoiceTarget("code-128", "name-247")
        assertEquals(LenientFreightInvoiceTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientFreightInvoiceTarget("code-128", "name-247")
        try {
            item.moveTo(LenientFreightInvoiceTargetStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFreightInvoiceTargetStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientFreightInvoiceTarget("code-128", "name-247")
        item.status = LenientFreightInvoiceTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientFreightInvoiceTargetStatus.PLANNED) }
    }
}
