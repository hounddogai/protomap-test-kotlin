package com.acme.transport.freightpay.freightinvoice.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceWindowTest {
    @Test
    fun `starts in planned`() {
        val item = newFreightInvoiceWindow("code-97", "label-149")
        assertEquals(FreightInvoiceWindowStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newFreightInvoiceWindow("code-97", "label-149")
        try {
            item.moveTo(FreightInvoiceWindowStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceWindowStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFreightInvoiceWindow("code-97", "label-149")
        item.status = FreightInvoiceWindowStatus.CANCELLED
        assertFails("move") { item.moveTo(FreightInvoiceWindowStatus.PLANNED) }
    }
}
