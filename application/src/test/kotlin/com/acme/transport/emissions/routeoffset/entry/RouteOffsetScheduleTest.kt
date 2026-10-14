package com.acme.transport.emissions.routeoffset.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newRouteOffsetSchedule("code-878", "title-836")
        assertEquals(RouteOffsetScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newRouteOffsetSchedule("code-878", "title-836")
        try {
            item.moveTo(RouteOffsetScheduleStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetScheduleStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRouteOffsetSchedule("code-878", "title-836")
        item.status = RouteOffsetScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(RouteOffsetScheduleStatus.DRAFT) }
    }
}
