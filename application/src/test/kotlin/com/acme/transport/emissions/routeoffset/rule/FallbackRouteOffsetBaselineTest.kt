package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRouteOffsetBaselineTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackRouteOffsetBaseline("code-28", "name-416")
        assertEquals(FallbackRouteOffsetBaselineStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackRouteOffsetBaseline("code-28", "name-416")
        try {
            item.moveTo(FallbackRouteOffsetBaselineStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRouteOffsetBaselineStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackRouteOffsetBaseline("code-28", "name-416")
        item.status = FallbackRouteOffsetBaselineStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackRouteOffsetBaselineStatus.DRAFT) }
    }
}
