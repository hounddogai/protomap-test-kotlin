package com.acme.transport.freightpay.freightinvoice.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyFreightInvoiceNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyFreightInvoiceNotice("code-918", "title-989")
        assertEquals(LegacyFreightInvoiceNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacyFreightInvoiceNotice("code-918", "title-989")
        try {
            item.moveTo(LegacyFreightInvoiceNoticeStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyFreightInvoiceNoticeStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyFreightInvoiceNotice("code-918", "title-989")
        item.status = LegacyFreightInvoiceNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyFreightInvoiceNoticeStatus.PLANNED) }
    }
}
