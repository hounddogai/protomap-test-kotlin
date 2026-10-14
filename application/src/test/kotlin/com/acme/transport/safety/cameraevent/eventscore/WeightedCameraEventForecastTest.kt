package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCameraEventForecastTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedCameraEventForecast("code-362", "title-223")
        assertEquals(WeightedCameraEventForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newWeightedCameraEventForecast("code-362", "title-223")
        try {
            item.moveTo(WeightedCameraEventForecastStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCameraEventForecastStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedCameraEventForecast("code-362", "title-223")
        item.status = WeightedCameraEventForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedCameraEventForecastStatus.OPEN) }
    }
}
