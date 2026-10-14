package com.acme.transport.safety.cameraevent.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventMetricTest {
    @Test
    fun `starts in created`() {
        val item = newCameraEventMetric("code-649", "name-99")
        assertEquals(CameraEventMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCameraEventMetric("code-649", "name-99")
        try {
            item.moveTo(CameraEventMetricStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventMetricStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCameraEventMetric("code-649", "name-99")
        item.status = CameraEventMetricStatus.RETIRED
        assertFails("move") { item.moveTo(CameraEventMetricStatus.CREATED) }
    }
}
