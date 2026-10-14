package com.acme.transport.linehaul.laneplan.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newLanePlanMetric("code-58", "name-679")
        assertEquals(LanePlanMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLanePlanMetric("code-58", "name-679")
        try {
            item.moveTo(LanePlanMetricStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanMetricStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLanePlanMetric("code-58", "name-679")
        item.status = LanePlanMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LanePlanMetricStatus.PROPOSED) }
    }
}
