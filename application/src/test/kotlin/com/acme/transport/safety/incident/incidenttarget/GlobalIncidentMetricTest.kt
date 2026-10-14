package com.acme.transport.safety.incident.incidenttarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalIncidentMetricTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalIncidentMetric("code-338", "title-70")
        assertEquals(GlobalIncidentMetricStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newGlobalIncidentMetric("code-338", "title-70")
        try {
            item.moveTo(GlobalIncidentMetricStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalIncidentMetricStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalIncidentMetric("code-338", "title-70")
        item.status = GlobalIncidentMetricStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalIncidentMetricStatus.CREATED) }
    }
}
