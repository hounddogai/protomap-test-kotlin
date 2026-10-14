package com.acme.transport.emissions.routeoffset.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRouteOffsetTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedRouteOffsetTarget("code-236", "title-218")
        val view = toWeightedRouteOffsetTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedRouteOffsetTargetViews(mutableListOf<WeightedRouteOffsetTarget>(item, item)).size, "views")
    }
}
