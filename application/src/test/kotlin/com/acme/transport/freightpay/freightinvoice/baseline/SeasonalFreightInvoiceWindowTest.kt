package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalFreightInvoiceWindowTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalFreightInvoiceWindow("code-897", "name-255")
        assertEquals(SeasonalFreightInvoiceWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newSeasonalFreightInvoiceWindow("code-897", "name-255")
        try {
            item.moveTo(SeasonalFreightInvoiceWindowStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalFreightInvoiceWindowStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalFreightInvoiceWindow("code-897", "name-255")
        item.status = SeasonalFreightInvoiceWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalFreightInvoiceWindowStatus.OPEN) }
    }
}
