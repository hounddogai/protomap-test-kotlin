package com.acme.transport.freightpay.freightinvoice.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newFreightInvoiceNotice("code-161", "name-390")
        assertEquals(FreightInvoiceNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFreightInvoiceNotice("code-161", "name-390")
        try {
            item.moveTo(FreightInvoiceNoticeStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceNoticeStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFreightInvoiceNotice("code-161", "name-390")
        item.status = FreightInvoiceNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(FreightInvoiceNoticeStatus.IDLE) }
    }
}
