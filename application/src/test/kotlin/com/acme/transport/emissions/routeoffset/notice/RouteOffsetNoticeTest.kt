package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetNoticeTest {
    @Test
    fun `starts in planned`() {
        val item = newRouteOffsetNotice("code-500", "label-475")
        assertEquals(RouteOffsetNoticeStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRouteOffsetNotice("code-500", "label-475")
        try {
            item.moveTo(RouteOffsetNoticeStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetNoticeStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRouteOffsetNotice("code-500", "label-475")
        item.status = RouteOffsetNoticeStatus.CANCELLED
        assertFails("move") { item.moveTo(RouteOffsetNoticeStatus.PLANNED) }
    }
}
