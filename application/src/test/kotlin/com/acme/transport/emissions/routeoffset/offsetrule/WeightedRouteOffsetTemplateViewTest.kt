package com.acme.transport.emissions.routeoffset.offsetrule

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRouteOffsetTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedRouteOffsetTemplate("code-252", "title-672")
        val view = toWeightedRouteOffsetTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedRouteOffsetTemplateViews(mutableListOf<WeightedRouteOffsetTemplate>(item, item)).size, "views")
    }
}
