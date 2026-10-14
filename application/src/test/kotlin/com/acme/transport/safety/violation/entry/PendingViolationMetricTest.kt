package com.acme.transport.safety.violation.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingViolationMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingViolationMetric("code-711", "name-668")
        assertEquals(PendingViolationMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPendingViolationMetric("code-711", "name-668")
        try {
            item.moveTo(PendingViolationMetricStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingViolationMetricStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingViolationMetric("code-711", "name-668")
        item.status = PendingViolationMetricStatus.CLOSED
        assertFails("move") { item.moveTo(PendingViolationMetricStatus.DRAFT) }
    }
}
