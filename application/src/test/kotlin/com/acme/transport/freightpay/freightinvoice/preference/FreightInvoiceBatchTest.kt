package com.acme.transport.freightpay.freightinvoice.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newFreightInvoiceBatch("code-425", "title-578")
        assertEquals(FreightInvoiceBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newFreightInvoiceBatch("code-425", "title-578")
        try {
            item.moveTo(FreightInvoiceBatchStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceBatchStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFreightInvoiceBatch("code-425", "title-578")
        item.status = FreightInvoiceBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(FreightInvoiceBatchStatus.REQUESTED) }
    }
}
