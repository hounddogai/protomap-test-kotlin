package com.acme.transport.freightpay.freightinvoice.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceGroupTest {
    @Test
    fun `starts in requested`() {
        val item = newFreightInvoiceGroup("code-646", "label-405")
        assertEquals(FreightInvoiceGroupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newFreightInvoiceGroup("code-646", "label-405")
        try {
            item.moveTo(FreightInvoiceGroupStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceGroupStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFreightInvoiceGroup("code-646", "label-405")
        item.status = FreightInvoiceGroupStatus.EXPIRED
        assertFails("move") { item.moveTo(FreightInvoiceGroupStatus.REQUESTED) }
    }
}
