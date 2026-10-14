package com.acme.transport.emissions.routeoffset.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRouteOffsetDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRouteOffsetDigest("code-811", "name-822")
        val view = toBatchedRouteOffsetDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRouteOffsetDigestViews(mutableListOf<BatchedRouteOffsetDigest>(item, item)).size, "views")
    }
}
