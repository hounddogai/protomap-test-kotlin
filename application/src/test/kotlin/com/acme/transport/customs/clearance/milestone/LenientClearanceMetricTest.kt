package com.acme.transport.customs.clearance.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientClearanceMetricTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientClearanceMetric("code-216", "title-203")
        assertEquals(LenientClearanceMetricStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientClearanceMetric("code-216", "title-203")
        try {
            item.moveTo(LenientClearanceMetricStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientClearanceMetricStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientClearanceMetric("code-216", "title-203")
        item.status = LenientClearanceMetricStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientClearanceMetricStatus.PLANNED) }
    }
}
