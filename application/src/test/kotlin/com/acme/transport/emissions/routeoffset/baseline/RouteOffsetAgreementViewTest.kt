package com.acme.transport.emissions.routeoffset.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetAgreement("code-45", "title-683")
        val view = toRouteOffsetAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetAgreementViews(mutableListOf<RouteOffsetAgreement>(item, item)).size, "views")
    }
}
