package com.acme.transport.emissions.routeoffset.offsetmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetDigest("code-423", "label-514")
        val view = toRouteOffsetDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetDigestViews(mutableListOf<RouteOffsetDigest>(item, item)).size, "views")
    }
}
