package com.acme.transport.customs.broker.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newBrokerForecast("code-719", "name-620")
        assertEquals(BrokerForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newBrokerForecast("code-719", "name-620")
        try {
            item.moveTo(BrokerForecastStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerForecastStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBrokerForecast("code-719", "name-620")
        item.status = BrokerForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(BrokerForecastStatus.PLANNED) }
    }
}
