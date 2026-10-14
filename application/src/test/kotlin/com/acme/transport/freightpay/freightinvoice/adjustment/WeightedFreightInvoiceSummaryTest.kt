package com.acme.transport.freightpay.freightinvoice.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFreightInvoiceSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedFreightInvoiceSummary("code-580", "name-288")
        assertEquals(WeightedFreightInvoiceSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newWeightedFreightInvoiceSummary("code-580", "name-288")
        try {
            item.moveTo(WeightedFreightInvoiceSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFreightInvoiceSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedFreightInvoiceSummary("code-580", "name-288")
        item.status = WeightedFreightInvoiceSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedFreightInvoiceSummaryStatus.PROPOSED) }
    }
}
