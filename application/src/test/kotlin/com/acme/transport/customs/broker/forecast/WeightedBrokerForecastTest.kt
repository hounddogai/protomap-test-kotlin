package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedBrokerForecastTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedBrokerForecast("code-53", "label-303")
        assertEquals(WeightedBrokerForecastStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newWeightedBrokerForecast("code-53", "label-303")
        try {
            item.moveTo(WeightedBrokerForecastStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedBrokerForecastStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedBrokerForecast("code-53", "label-303")
        item.status = WeightedBrokerForecastStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedBrokerForecastStatus.DRAFT) }
    }
}
