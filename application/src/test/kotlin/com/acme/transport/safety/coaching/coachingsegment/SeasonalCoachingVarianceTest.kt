package com.acme.transport.safety.coaching.coachingsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalCoachingVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalCoachingVariance("code-335", "label-154")
        assertEquals(SeasonalCoachingVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newSeasonalCoachingVariance("code-335", "label-154")
        try {
            item.moveTo(SeasonalCoachingVarianceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalCoachingVarianceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalCoachingVariance("code-335", "label-154")
        item.status = SeasonalCoachingVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalCoachingVarianceStatus.PLANNED) }
    }
}
