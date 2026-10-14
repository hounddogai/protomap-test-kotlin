package com.acme.transport.freightpay.freightinvoice.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newFreightInvoiceForecast("code-52", "name-681")
        assertEquals(FreightInvoiceForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newFreightInvoiceForecast("code-52", "name-681")
        try {
            item.moveTo(FreightInvoiceForecastStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceForecastStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFreightInvoiceForecast("code-52", "name-681")
        item.status = FreightInvoiceForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FreightInvoiceForecastStatus.PROPOSED) }
    }
}
