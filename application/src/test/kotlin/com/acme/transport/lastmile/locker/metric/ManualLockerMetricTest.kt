package com.acme.transport.lastmile.locker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualLockerMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newManualLockerMetric("code-296", "title-237")
        assertEquals(ManualLockerMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newManualLockerMetric("code-296", "title-237")
        try {
            item.moveTo(ManualLockerMetricStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualLockerMetricStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualLockerMetric("code-296", "title-237")
        item.status = ManualLockerMetricStatus.STOPPED
        assertFails("move") { item.moveTo(ManualLockerMetricStatus.IDLE) }
    }
}
