package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryRouteOffsetDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryRouteOffsetDigest("code-440", "name-29")
        val view = toPrimaryRouteOffsetDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryRouteOffsetDigestViews(mutableListOf<PrimaryRouteOffsetDigest>(item, item)).size, "views")
    }
}
