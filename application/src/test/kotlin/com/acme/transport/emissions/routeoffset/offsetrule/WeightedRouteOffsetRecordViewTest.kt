package com.acme.transport.emissions.routeoffset.offsetrule

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedRouteOffsetRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedRouteOffsetRecord("code-588", "label-459")
        val view = toWeightedRouteOffsetRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedRouteOffsetRecordViews(mutableListOf<WeightedRouteOffsetRecord>(item, item)).size, "views")
    }
}
