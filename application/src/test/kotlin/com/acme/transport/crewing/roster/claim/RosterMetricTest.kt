package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newRosterMetric("code-566", "name-753")
        assertEquals(RosterMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRosterMetric("code-566", "name-753")
        try {
            item.moveTo(RosterMetricStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterMetricStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRosterMetric("code-566", "name-753")
        item.status = RosterMetricStatus.CLOSED
        assertFails("move") { item.moveTo(RosterMetricStatus.DRAFT) }
    }
}
