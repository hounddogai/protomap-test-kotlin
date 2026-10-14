package com.acme.transport.emissions.routeoffset.offsetmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetDigestTest {
    @Test
    fun `starts in open`() {
        val item = newRouteOffsetDigest("code-423", "label-514")
        assertEquals(RouteOffsetDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRouteOffsetDigest("code-423", "label-514")
        try {
            item.moveTo(RouteOffsetDigestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetDigestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRouteOffsetDigest("code-423", "label-514")
        item.status = RouteOffsetDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(RouteOffsetDigestStatus.OPEN) }
    }
}
