package com.acme.transport.freightpay.ratecard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRateCardSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyRateCardSummary("code-6", "name-758")
        assertEquals(LegacyRateCardSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLegacyRateCardSummary("code-6", "name-758")
        try {
            item.moveTo(LegacyRateCardSummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRateCardSummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyRateCardSummary("code-6", "name-758")
        item.status = LegacyRateCardSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyRateCardSummaryStatus.CREATED) }
    }
}
