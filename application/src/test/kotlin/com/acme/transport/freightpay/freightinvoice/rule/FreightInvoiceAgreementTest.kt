package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newFreightInvoiceAgreement("code-873", "name-987")
        assertEquals(FreightInvoiceAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFreightInvoiceAgreement("code-873", "name-987")
        try {
            item.moveTo(FreightInvoiceAgreementStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceAgreementStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFreightInvoiceAgreement("code-873", "name-987")
        item.status = FreightInvoiceAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(FreightInvoiceAgreementStatus.CREATED) }
    }
}
