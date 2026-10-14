package com.acme.transport.network.cutoff.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCutoffForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedCutoffForecast("code-511", "name-218")
        assertEquals(WeightedCutoffForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedCutoffForecast("code-511", "name-218")
        try {
            item.moveTo(WeightedCutoffForecastStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCutoffForecastStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedCutoffForecast("code-511", "name-218")
        item.status = WeightedCutoffForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedCutoffForecastStatus.PLANNED) }
    }
}
