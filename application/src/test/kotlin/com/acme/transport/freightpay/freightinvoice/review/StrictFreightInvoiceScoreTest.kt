package com.acme.transport.freightpay.freightinvoice.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictFreightInvoiceScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictFreightInvoiceScore("code-499", "title-238")
        assertEquals(StrictFreightInvoiceScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newStrictFreightInvoiceScore("code-499", "title-238")
        try {
            item.moveTo(StrictFreightInvoiceScoreStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictFreightInvoiceScoreStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictFreightInvoiceScore("code-499", "title-238")
        item.status = StrictFreightInvoiceScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictFreightInvoiceScoreStatus.PLANNED) }
    }
}
