package com.acme.transport.safety.nearmiss.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultNearMissMetricTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultNearMissMetric("code-721", "title-96")
        assertEquals(DefaultNearMissMetricStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDefaultNearMissMetric("code-721", "title-96")
        try {
            item.moveTo(DefaultNearMissMetricStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultNearMissMetricStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultNearMissMetric("code-721", "title-96")
        item.status = DefaultNearMissMetricStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultNearMissMetricStatus.PROPOSED) }
    }
}
