package com.acme.transport.freightpay.ratecard.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRateCardSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingRateCardSummary("code-311", "label-851")
        assertEquals(RollingRateCardSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingRateCardSummary("code-311", "label-851")
        try {
            item.moveTo(RollingRateCardSummaryStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRateCardSummaryStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingRateCardSummary("code-311", "label-851")
        item.status = RollingRateCardSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingRateCardSummaryStatus.PLANNED) }
    }
}
