package com.acme.transport.freightpay.freightinvoice.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalFreightInvoiceThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalFreightInvoiceThreshold("code-279", "title-347")
        assertEquals(RegionalFreightInvoiceThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalFreightInvoiceThreshold("code-279", "title-347")
        try {
            item.moveTo(RegionalFreightInvoiceThresholdStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalFreightInvoiceThresholdStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalFreightInvoiceThreshold("code-279", "title-347")
        item.status = RegionalFreightInvoiceThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalFreightInvoiceThresholdStatus.REQUESTED) }
    }
}
