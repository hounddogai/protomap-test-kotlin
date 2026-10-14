package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newRouteOffsetException("code-614", "title-402")
        assertEquals(RouteOffsetExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRouteOffsetException("code-614", "title-402")
        try {
            item.moveTo(RouteOffsetExceptionStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetExceptionStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRouteOffsetException("code-614", "title-402")
        item.status = RouteOffsetExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(RouteOffsetExceptionStatus.IDLE) }
    }
}
