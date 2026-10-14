package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultAttemptMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultAttemptMetric("code-403", "name-240")
        assertEquals(DefaultAttemptMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newDefaultAttemptMetric("code-403", "name-240")
        try {
            item.moveTo(DefaultAttemptMetricStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultAttemptMetricStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultAttemptMetric("code-403", "name-240")
        item.status = DefaultAttemptMetricStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultAttemptMetricStatus.DRAFT) }
    }
}
