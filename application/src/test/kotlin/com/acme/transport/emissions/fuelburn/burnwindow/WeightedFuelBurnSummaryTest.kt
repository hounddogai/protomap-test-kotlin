package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedFuelBurnSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedFuelBurnSummary("code-443", "label-364")
        assertEquals(WeightedFuelBurnSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newWeightedFuelBurnSummary("code-443", "label-364")
        try {
            item.moveTo(WeightedFuelBurnSummaryStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedFuelBurnSummaryStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedFuelBurnSummary("code-443", "label-364")
        item.status = WeightedFuelBurnSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedFuelBurnSummaryStatus.PROPOSED) }
    }
}
