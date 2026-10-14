package com.acme.transport.freightpay.freightinvoice.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceScoreTest {
    @Test
    fun `starts in requested`() {
        val item = newFreightInvoiceScore("code-910", "name-437")
        assertEquals(FreightInvoiceScoreStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFreightInvoiceScore("code-910", "name-437")
        try {
            item.moveTo(FreightInvoiceScoreStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceScoreStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFreightInvoiceScore("code-910", "name-437")
        item.status = FreightInvoiceScoreStatus.EXPIRED
        assertFails("move") { item.moveTo(FreightInvoiceScoreStatus.REQUESTED) }
    }
}
