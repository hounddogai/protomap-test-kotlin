package com.acme.transport.safety.cameraevent.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedCameraEventForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newBatchedCameraEventForecast("code-203", "name-378")
        assertEquals(BatchedCameraEventForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newBatchedCameraEventForecast("code-203", "name-378")
        try {
            item.moveTo(BatchedCameraEventForecastStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedCameraEventForecastStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBatchedCameraEventForecast("code-203", "name-378")
        item.status = BatchedCameraEventForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(BatchedCameraEventForecastStatus.PLANNED) }
    }
}
