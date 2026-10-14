package com.acme.transport.terminal.dockdoor.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDockDoorMetricTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalDockDoorMetric("code-414", "title-368")
        assertEquals(RegionalDockDoorMetricStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRegionalDockDoorMetric("code-414", "title-368")
        try {
            item.moveTo(RegionalDockDoorMetricStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDockDoorMetricStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalDockDoorMetric("code-414", "title-368")
        item.status = RegionalDockDoorMetricStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalDockDoorMetricStatus.IDLE) }
    }
}
