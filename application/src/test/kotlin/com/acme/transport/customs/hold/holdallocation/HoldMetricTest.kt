package com.acme.transport.customs.hold.holdallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newHoldMetric("code-692", "name-273")
        assertEquals(HoldMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newHoldMetric("code-692", "name-273")
        try {
            item.moveTo(HoldMetricStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldMetricStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newHoldMetric("code-692", "name-273")
        item.status = HoldMetricStatus.STOPPED
        assertFails("move") { item.moveTo(HoldMetricStatus.IDLE) }
    }
}
