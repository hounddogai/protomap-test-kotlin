package com.acme.transport.emissions.routeoffset.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalRouteOffsetMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalRouteOffsetMapping("code-319", "title-831")
        val view = toRegionalRouteOffsetMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalRouteOffsetMappingViews(mutableListOf<RegionalRouteOffsetMapping>(item, item)).size, "views")
    }
}
