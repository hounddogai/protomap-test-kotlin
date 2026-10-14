package com.acme.transport.terminal.dockdoor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DockDoorMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newDockDoorMetric("code-90", "name-329")
        assertEquals(DockDoorMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDockDoorMetric("code-90", "name-329")
        try {
            item.moveTo(DockDoorMetricStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DockDoorMetricStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDockDoorMetric("code-90", "name-329")
        item.status = DockDoorMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DockDoorMetricStatus.PROPOSED) }
    }
}
