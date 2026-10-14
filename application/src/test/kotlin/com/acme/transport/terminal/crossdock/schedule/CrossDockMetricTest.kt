package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newCrossDockMetric("code-284", "label-808")
        assertEquals(CrossDockMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newCrossDockMetric("code-284", "label-808")
        try {
            item.moveTo(CrossDockMetricStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockMetricStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCrossDockMetric("code-284", "label-808")
        item.status = CrossDockMetricStatus.CLOSED
        assertFails("move") { item.moveTo(CrossDockMetricStatus.DRAFT) }
    }
}
