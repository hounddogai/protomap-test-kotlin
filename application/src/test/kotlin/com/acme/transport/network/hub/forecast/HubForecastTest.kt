package com.acme.transport.network.hub.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newHubForecast("code-547", "name-960")
        assertEquals(HubForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newHubForecast("code-547", "name-960")
        try {
            item.moveTo(HubForecastStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubForecastStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newHubForecast("code-547", "name-960")
        item.status = HubForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(HubForecastStatus.PROPOSED) }
    }
}
