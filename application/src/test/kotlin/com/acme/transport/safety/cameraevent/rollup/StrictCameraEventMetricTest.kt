package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCameraEventMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictCameraEventMetric("code-254", "title-29")
        assertEquals(StrictCameraEventMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newStrictCameraEventMetric("code-254", "title-29")
        try {
            item.moveTo(StrictCameraEventMetricStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCameraEventMetricStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictCameraEventMetric("code-254", "title-29")
        item.status = StrictCameraEventMetricStatus.STOPPED
        assertFails("move") { item.moveTo(StrictCameraEventMetricStatus.IDLE) }
    }
}
