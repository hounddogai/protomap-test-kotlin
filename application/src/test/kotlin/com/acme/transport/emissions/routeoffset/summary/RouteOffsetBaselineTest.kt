package com.acme.transport.emissions.routeoffset.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetBaselineTest {
    @Test
    fun `starts in new`() {
        val item = newRouteOffsetBaseline("code-207", "title-922")
        assertEquals(RouteOffsetBaselineStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRouteOffsetBaseline("code-207", "title-922")
        try {
            item.moveTo(RouteOffsetBaselineStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetBaselineStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRouteOffsetBaseline("code-207", "title-922")
        item.status = RouteOffsetBaselineStatus.FAILED
        assertFails("move") { item.moveTo(RouteOffsetBaselineStatus.NEW) }
    }
}
