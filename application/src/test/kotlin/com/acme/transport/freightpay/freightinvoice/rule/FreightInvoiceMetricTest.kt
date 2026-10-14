package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newFreightInvoiceMetric("code-246", "name-379")
        assertEquals(FreightInvoiceMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFreightInvoiceMetric("code-246", "name-379")
        try {
            item.moveTo(FreightInvoiceMetricStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceMetricStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFreightInvoiceMetric("code-246", "name-379")
        item.status = FreightInvoiceMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FreightInvoiceMetricStatus.PROPOSED) }
    }
}
