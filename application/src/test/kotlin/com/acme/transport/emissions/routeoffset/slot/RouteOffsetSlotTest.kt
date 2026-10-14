package com.acme.transport.emissions.routeoffset.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetSlotTest {
    @Test
    fun `starts in requested`() {
        val item = newRouteOffsetSlot("code-358", "title-168")
        assertEquals(RouteOffsetSlotStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRouteOffsetSlot("code-358", "title-168")
        try {
            item.moveTo(RouteOffsetSlotStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetSlotStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRouteOffsetSlot("code-358", "title-168")
        item.status = RouteOffsetSlotStatus.EXPIRED
        assertFails("move") { item.moveTo(RouteOffsetSlotStatus.REQUESTED) }
    }
}
