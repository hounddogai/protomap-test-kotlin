package com.acme.transport.emissions.routeoffset.batchedoffsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRouteOffsetQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRouteOffsetQuota("code-80", "label-776")
        val view = toGlobalRouteOffsetQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRouteOffsetQuotaViews(mutableListOf<GlobalRouteOffsetQuota>(item, item)).size, "views")
    }
}
