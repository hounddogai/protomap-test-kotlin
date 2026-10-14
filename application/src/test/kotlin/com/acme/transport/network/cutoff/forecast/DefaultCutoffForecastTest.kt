package com.acme.transport.network.cutoff.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCutoffForecastTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultCutoffForecast("code-98", "label-565")
        assertEquals(DefaultCutoffForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newDefaultCutoffForecast("code-98", "label-565")
        try {
            item.moveTo(DefaultCutoffForecastStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCutoffForecastStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultCutoffForecast("code-98", "label-565")
        item.status = DefaultCutoffForecastStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultCutoffForecastStatus.CREATED) }
    }
}
