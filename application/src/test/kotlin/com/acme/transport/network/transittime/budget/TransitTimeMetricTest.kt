package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newTransitTimeMetric("code-830", "title-585")
        assertEquals(TransitTimeMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newTransitTimeMetric("code-830", "title-585")
        try {
            item.moveTo(TransitTimeMetricStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeMetricStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTransitTimeMetric("code-830", "title-585")
        item.status = TransitTimeMetricStatus.CLOSED
        assertFails("move") { item.moveTo(TransitTimeMetricStatus.DRAFT) }
    }
}
