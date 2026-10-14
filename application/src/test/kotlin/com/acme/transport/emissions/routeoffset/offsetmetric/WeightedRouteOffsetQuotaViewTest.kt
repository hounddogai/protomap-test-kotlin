package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRouteOffsetQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedRouteOffsetQuota("code-346", "label-296")
        val view = toWeightedRouteOffsetQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedRouteOffsetQuotaViews(mutableListOf<WeightedRouteOffsetQuota>(item, item)).size, "views")
    }
}
