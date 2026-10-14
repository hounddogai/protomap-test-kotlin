package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCoachingMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalCoachingMetric("code-501", "label-269")
        assertEquals(SeasonalCoachingMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newSeasonalCoachingMetric("code-501", "label-269")
        try {
            item.moveTo(SeasonalCoachingMetricStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCoachingMetricStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalCoachingMetric("code-501", "label-269")
        item.status = SeasonalCoachingMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalCoachingMetricStatus.REQUESTED) }
    }
}
