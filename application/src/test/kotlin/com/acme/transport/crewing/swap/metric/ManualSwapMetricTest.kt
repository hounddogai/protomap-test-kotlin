package com.acme.transport.crewing.swap.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualSwapMetricTest {
    @Test
    fun `starts in new`() {
        val item = newManualSwapMetric("code-21", "label-85")
        assertEquals(ManualSwapMetricStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newManualSwapMetric("code-21", "label-85")
        try {
            item.moveTo(ManualSwapMetricStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualSwapMetricStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualSwapMetric("code-21", "label-85")
        item.status = ManualSwapMetricStatus.FAILED
        assertFails("move") { item.moveTo(ManualSwapMetricStatus.NEW) }
    }
}
