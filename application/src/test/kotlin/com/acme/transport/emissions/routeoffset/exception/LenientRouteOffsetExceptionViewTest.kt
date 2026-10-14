package com.acme.transport.emissions.routeoffset.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientRouteOffsetExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientRouteOffsetException("code-944", "label-519")
        val view = toLenientRouteOffsetExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientRouteOffsetExceptionViews(mutableListOf<LenientRouteOffsetException>(item, item)).size, "views")
    }
}
