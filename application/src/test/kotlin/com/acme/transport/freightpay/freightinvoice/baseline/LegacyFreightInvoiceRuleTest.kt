package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyFreightInvoiceRuleTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyFreightInvoiceRule("code-995", "title-287")
        assertEquals(LegacyFreightInvoiceRuleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLegacyFreightInvoiceRule("code-995", "title-287")
        try {
            item.moveTo(LegacyFreightInvoiceRuleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyFreightInvoiceRuleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyFreightInvoiceRule("code-995", "title-287")
        item.status = LegacyFreightInvoiceRuleStatus.FAILED
        assertFails("move") { item.moveTo(LegacyFreightInvoiceRuleStatus.NEW) }
    }
}
