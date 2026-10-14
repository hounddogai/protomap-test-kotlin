package com.acme.transport.emissions.routeoffset.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetTierTest {
    @Test
    fun `starts in idle`() {
        val item = newRouteOffsetTier("code-948", "name-558")
        assertEquals(RouteOffsetTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRouteOffsetTier("code-948", "name-558")
        try {
            item.moveTo(RouteOffsetTierStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetTierStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRouteOffsetTier("code-948", "name-558")
        item.status = RouteOffsetTierStatus.STOPPED
        assertFails("move") { item.moveTo(RouteOffsetTierStatus.IDLE) }
    }
}
