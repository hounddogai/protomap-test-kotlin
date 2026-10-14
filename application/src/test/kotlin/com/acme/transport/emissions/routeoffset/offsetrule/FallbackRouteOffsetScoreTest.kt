package com.acme.transport.emissions.routeoffset.offsetrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRouteOffsetScoreTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackRouteOffsetScore("code-151", "title-420")
        assertEquals(FallbackRouteOffsetScoreStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFallbackRouteOffsetScore("code-151", "title-420")
        try {
            item.moveTo(FallbackRouteOffsetScoreStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRouteOffsetScoreStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackRouteOffsetScore("code-151", "title-420")
        item.status = FallbackRouteOffsetScoreStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackRouteOffsetScoreStatus.PLANNED) }
    }
}
