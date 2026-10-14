package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newFreightInvoiceBaseline("code-150", "label-216")
        assertEquals(FreightInvoiceBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFreightInvoiceBaseline("code-150", "label-216")
        try {
            item.moveTo(FreightInvoiceBaselineStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceBaselineStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFreightInvoiceBaseline("code-150", "label-216")
        item.status = FreightInvoiceBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(FreightInvoiceBaselineStatus.IDLE) }
    }
}
