package com.acme.transport.emissions.routeoffset.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newRouteOffsetProfile("code-145", "label-267")
        assertEquals(RouteOffsetProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRouteOffsetProfile("code-145", "label-267")
        try {
            item.moveTo(RouteOffsetProfileStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetProfileStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRouteOffsetProfile("code-145", "label-267")
        item.status = RouteOffsetProfileStatus.CLOSED
        assertFails("move") { item.moveTo(RouteOffsetProfileStatus.DRAFT) }
    }
}
