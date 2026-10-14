package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRateCardSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newManualRateCardSummary("code-353", "label-888")
        assertEquals(ManualRateCardSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newManualRateCardSummary("code-353", "label-888")
        try {
            item.moveTo(ManualRateCardSummaryStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRateCardSummaryStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualRateCardSummary("code-353", "label-888")
        item.status = ManualRateCardSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualRateCardSummaryStatus.REQUESTED) }
    }
}
