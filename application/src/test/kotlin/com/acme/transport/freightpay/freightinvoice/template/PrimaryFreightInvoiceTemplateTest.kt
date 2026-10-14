package com.acme.transport.freightpay.freightinvoice.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryFreightInvoiceTemplateTest {
    @Test
    fun `starts in open`() {
        val item = newPrimaryFreightInvoiceTemplate("code-429", "name-515")
        assertEquals(PrimaryFreightInvoiceTemplateStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newPrimaryFreightInvoiceTemplate("code-429", "name-515")
        try {
            item.moveTo(PrimaryFreightInvoiceTemplateStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryFreightInvoiceTemplateStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newPrimaryFreightInvoiceTemplate("code-429", "name-515")
        item.status = PrimaryFreightInvoiceTemplateStatus.ARCHIVED
        assertFails("move") { item.moveTo(PrimaryFreightInvoiceTemplateStatus.OPEN) }
    }
}
