package com.acme.transport.freightpay.freightinvoice.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveFreightInvoiceAgreementTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveFreightInvoiceAgreement("code-385", "title-127")
        assertEquals(AdaptiveFreightInvoiceAgreementStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newAdaptiveFreightInvoiceAgreement("code-385", "title-127")
        try {
            item.moveTo(AdaptiveFreightInvoiceAgreementStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveFreightInvoiceAgreementStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveFreightInvoiceAgreement("code-385", "title-127")
        item.status = AdaptiveFreightInvoiceAgreementStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveFreightInvoiceAgreementStatus.PLANNED) }
    }
}
