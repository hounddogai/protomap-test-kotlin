package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackServiceAreaForecastTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackServiceAreaForecast("code-408", "title-678")
        assertEquals(FallbackServiceAreaForecastStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFallbackServiceAreaForecast("code-408", "title-678")
        try {
            item.moveTo(FallbackServiceAreaForecastStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackServiceAreaForecastStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackServiceAreaForecast("code-408", "title-678")
        item.status = FallbackServiceAreaForecastStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackServiceAreaForecastStatus.CREATED) }
    }
}
