package com.acme.transport.network.servicearea.areathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredServiceAreaForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredServiceAreaForecast("code-643", "label-80")
        assertEquals(TieredServiceAreaForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredServiceAreaForecast("code-643", "label-80")
        try {
            item.moveTo(TieredServiceAreaForecastStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredServiceAreaForecastStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredServiceAreaForecast("code-643", "label-80")
        item.status = TieredServiceAreaForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredServiceAreaForecastStatus.PROPOSED) }
    }
}
