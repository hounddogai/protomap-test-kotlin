package com.acme.transport.linehaul.capacity.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCapacityMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalCapacityMetric("code-937", "title-319")
        assertEquals(SeasonalCapacityMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newSeasonalCapacityMetric("code-937", "title-319")
        try {
            item.moveTo(SeasonalCapacityMetricStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCapacityMetricStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalCapacityMetric("code-937", "title-319")
        item.status = SeasonalCapacityMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalCapacityMetricStatus.PROPOSED) }
    }
}
