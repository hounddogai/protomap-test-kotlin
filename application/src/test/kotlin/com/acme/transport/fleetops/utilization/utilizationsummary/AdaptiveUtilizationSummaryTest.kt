package com.acme.transport.fleetops.utilization.utilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveUtilizationSummaryTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveUtilizationSummary("code-34", "name-565")
        assertEquals(AdaptiveUtilizationSummaryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newAdaptiveUtilizationSummary("code-34", "name-565")
        try {
            item.moveTo(AdaptiveUtilizationSummaryStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveUtilizationSummaryStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveUtilizationSummary("code-34", "name-565")
        item.status = AdaptiveUtilizationSummaryStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveUtilizationSummaryStatus.PLANNED) }
    }
}
