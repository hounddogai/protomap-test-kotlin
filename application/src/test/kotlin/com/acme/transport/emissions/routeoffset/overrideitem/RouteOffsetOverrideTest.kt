package com.acme.transport.emissions.routeoffset.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newRouteOffsetOverride("code-736", "title-93")
        assertEquals(RouteOffsetOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRouteOffsetOverride("code-736", "title-93")
        try {
            item.moveTo(RouteOffsetOverrideStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetOverrideStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRouteOffsetOverride("code-736", "title-93")
        item.status = RouteOffsetOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(RouteOffsetOverrideStatus.OPEN) }
    }
}
