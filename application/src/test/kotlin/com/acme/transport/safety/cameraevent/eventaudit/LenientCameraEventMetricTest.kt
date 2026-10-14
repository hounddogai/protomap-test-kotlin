package com.acme.transport.safety.cameraevent.eventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCameraEventMetricTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientCameraEventMetric("code-39", "title-116")
        assertEquals(LenientCameraEventMetricStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientCameraEventMetric("code-39", "title-116")
        try {
            item.moveTo(LenientCameraEventMetricStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCameraEventMetricStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientCameraEventMetric("code-39", "title-116")
        item.status = LenientCameraEventMetricStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientCameraEventMetricStatus.PLANNED) }
    }
}
