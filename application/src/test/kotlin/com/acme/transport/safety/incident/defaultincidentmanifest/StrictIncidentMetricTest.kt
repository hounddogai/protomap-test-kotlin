package com.acme.transport.safety.incident.defaultincidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictIncidentMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictIncidentMetric("code-776", "label-102")
        assertEquals(StrictIncidentMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newStrictIncidentMetric("code-776", "label-102")
        try {
            item.moveTo(StrictIncidentMetricStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictIncidentMetricStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictIncidentMetric("code-776", "label-102")
        item.status = StrictIncidentMetricStatus.CLOSED
        assertFails("move") { item.moveTo(StrictIncidentMetricStatus.DRAFT) }
    }
}
