package com.acme.transport.emissions.routeoffset.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetGroupTest {
    @Test
    fun `starts in idle`() {
        val item = newRouteOffsetGroup("code-914", "title-369")
        assertEquals(RouteOffsetGroupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newRouteOffsetGroup("code-914", "title-369")
        try {
            item.moveTo(RouteOffsetGroupStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetGroupStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRouteOffsetGroup("code-914", "title-369")
        item.status = RouteOffsetGroupStatus.STOPPED
        assertFails("move") { item.moveTo(RouteOffsetGroupStatus.IDLE) }
    }
}
