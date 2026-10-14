package com.acme.transport.emissions.routeoffset.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetManifestTest {
    @Test
    fun `starts in open`() {
        val item = newRouteOffsetManifest("code-579", "label-403")
        assertEquals(RouteOffsetManifestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRouteOffsetManifest("code-579", "label-403")
        try {
            item.moveTo(RouteOffsetManifestStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetManifestStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRouteOffsetManifest("code-579", "label-403")
        item.status = RouteOffsetManifestStatus.ARCHIVED
        assertFails("move") { item.moveTo(RouteOffsetManifestStatus.OPEN) }
    }
}
