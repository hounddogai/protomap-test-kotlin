package com.acme.transport.emissions.routeoffset.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRouteOffsetVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackRouteOffsetVariance("code-507", "title-829")
        assertEquals(FallbackRouteOffsetVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFallbackRouteOffsetVariance("code-507", "title-829")
        try {
            item.moveTo(FallbackRouteOffsetVarianceStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRouteOffsetVarianceStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackRouteOffsetVariance("code-507", "title-829")
        item.status = FallbackRouteOffsetVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackRouteOffsetVarianceStatus.PLANNED) }
    }
}
