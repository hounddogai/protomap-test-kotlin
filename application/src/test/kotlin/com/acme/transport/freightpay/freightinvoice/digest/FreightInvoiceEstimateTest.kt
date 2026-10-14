package com.acme.transport.freightpay.freightinvoice.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceEstimateTest {
    @Test
    fun `starts in requested`() {
        val item = newFreightInvoiceEstimate("code-503", "name-67")
        assertEquals(FreightInvoiceEstimateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newFreightInvoiceEstimate("code-503", "name-67")
        try {
            item.moveTo(FreightInvoiceEstimateStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceEstimateStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFreightInvoiceEstimate("code-503", "name-67")
        item.status = FreightInvoiceEstimateStatus.EXPIRED
        assertFails("move") { item.moveTo(FreightInvoiceEstimateStatus.REQUESTED) }
    }
}
