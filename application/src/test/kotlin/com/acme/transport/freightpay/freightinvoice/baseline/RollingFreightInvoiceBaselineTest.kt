package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFreightInvoiceBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingFreightInvoiceBaseline("code-836", "title-987")
        assertEquals(RollingFreightInvoiceBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingFreightInvoiceBaseline("code-836", "title-987")
        try {
            item.moveTo(RollingFreightInvoiceBaselineStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFreightInvoiceBaselineStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingFreightInvoiceBaseline("code-836", "title-987")
        item.status = RollingFreightInvoiceBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(RollingFreightInvoiceBaselineStatus.DRAFT) }
    }
}
