package com.acme.transport.network.transittime.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryTransitTimeMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimaryTransitTimeMetric("code-421", "name-337")
        assertEquals(PrimaryTransitTimeMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newPrimaryTransitTimeMetric("code-421", "name-337")
        try {
            item.moveTo(PrimaryTransitTimeMetricStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryTransitTimeMetricStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimaryTransitTimeMetric("code-421", "name-337")
        item.status = PrimaryTransitTimeMetricStatus.CLOSED
        assertFails("move") { item.moveTo(PrimaryTransitTimeMetricStatus.DRAFT) }
    }
}
