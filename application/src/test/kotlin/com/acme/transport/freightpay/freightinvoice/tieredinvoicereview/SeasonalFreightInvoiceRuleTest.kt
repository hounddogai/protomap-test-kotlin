package com.acme.transport.freightpay.freightinvoice.tieredinvoicereview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalFreightInvoiceRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newSeasonalFreightInvoiceRule("code-157", "name-248")
        assertEquals(SeasonalFreightInvoiceRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newSeasonalFreightInvoiceRule("code-157", "name-248")
        try {
            item.moveTo(SeasonalFreightInvoiceRuleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalFreightInvoiceRuleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSeasonalFreightInvoiceRule("code-157", "name-248")
        item.status = SeasonalFreightInvoiceRuleStatus.STOPPED
        assertFails("move") { item.moveTo(SeasonalFreightInvoiceRuleStatus.IDLE) }
    }
}
