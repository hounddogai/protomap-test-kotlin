package com.acme.transport.safety.coaching.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCoachingMetricTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultCoachingMetric("code-656", "name-798")
        assertEquals(DefaultCoachingMetricStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newDefaultCoachingMetric("code-656", "name-798")
        try {
            item.moveTo(DefaultCoachingMetricStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCoachingMetricStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultCoachingMetric("code-656", "name-798")
        item.status = DefaultCoachingMetricStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultCoachingMetricStatus.REQUESTED) }
    }
}
