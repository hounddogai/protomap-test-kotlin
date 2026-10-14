package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingEvChargeMetricTest {
    @Test
    fun `starts in new`() {
        val item = newRollingEvChargeMetric("code-57", "label-639")
        assertEquals(RollingEvChargeMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRollingEvChargeMetric("code-57", "label-639")
        try {
            item.moveTo(RollingEvChargeMetricStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingEvChargeMetricStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingEvChargeMetric("code-57", "label-639")
        item.status = RollingEvChargeMetricStatus.FAILED
        assertFails("move") { item.moveTo(RollingEvChargeMetricStatus.NEW) }
    }
}
