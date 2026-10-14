package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceTargetTest {
    @Test
    fun `starts in created`() {
        val item = newFreightInvoiceTarget("code-714", "title-200")
        assertEquals(FreightInvoiceTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFreightInvoiceTarget("code-714", "title-200")
        try {
            item.moveTo(FreightInvoiceTargetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceTargetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFreightInvoiceTarget("code-714", "title-200")
        item.status = FreightInvoiceTargetStatus.RETIRED
        assertFails("move") { item.moveTo(FreightInvoiceTargetStatus.CREATED) }
    }
}
