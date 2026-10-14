package com.acme.transport.network.transittime.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTransitTimeBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedTransitTimeBaseline("code-902", "title-263")
        assertEquals(WeightedTransitTimeBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newWeightedTransitTimeBaseline("code-902", "title-263")
        try {
            item.moveTo(WeightedTransitTimeBaselineStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTransitTimeBaselineStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedTransitTimeBaseline("code-902", "title-263")
        item.status = WeightedTransitTimeBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedTransitTimeBaselineStatus.PLANNED) }
    }
}
