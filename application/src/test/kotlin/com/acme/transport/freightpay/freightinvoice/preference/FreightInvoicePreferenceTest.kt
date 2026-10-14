package com.acme.transport.freightpay.freightinvoice.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoicePreferenceTest {
    @Test
    fun `starts in created`() {
        val item = newFreightInvoicePreference("code-923", "label-115")
        assertEquals(FreightInvoicePreferenceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFreightInvoicePreference("code-923", "label-115")
        try {
            item.moveTo(FreightInvoicePreferenceStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoicePreferenceStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFreightInvoicePreference("code-923", "label-115")
        item.status = FreightInvoicePreferenceStatus.RETIRED
        assertFails("move") { item.moveTo(FreightInvoicePreferenceStatus.CREATED) }
    }
}
