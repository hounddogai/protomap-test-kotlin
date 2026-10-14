package com.acme.transport.safety.incident.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyIncidentMetricTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyIncidentMetric("code-401", "title-992")
        assertEquals(LegacyIncidentMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLegacyIncidentMetric("code-401", "title-992")
        try {
            item.moveTo(LegacyIncidentMetricStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyIncidentMetricStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyIncidentMetric("code-401", "title-992")
        item.status = LegacyIncidentMetricStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyIncidentMetricStatus.CREATED) }
    }
}
