package com.acme.transport.safety.coaching.coachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCoachingMetricTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalCoachingMetric("code-34", "name-316")
        assertEquals(GlobalCoachingMetricStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newGlobalCoachingMetric("code-34", "name-316")
        try {
            item.moveTo(GlobalCoachingMetricStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCoachingMetricStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalCoachingMetric("code-34", "name-316")
        item.status = GlobalCoachingMetricStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalCoachingMetricStatus.PLANNED) }
    }
}
