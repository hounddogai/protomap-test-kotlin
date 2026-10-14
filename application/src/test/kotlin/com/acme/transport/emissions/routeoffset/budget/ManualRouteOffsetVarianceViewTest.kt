package com.acme.transport.emissions.routeoffset.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualRouteOffsetVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualRouteOffsetVariance("code-565", "label-837")
        val view = toManualRouteOffsetVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualRouteOffsetVarianceViews(mutableListOf<ManualRouteOffsetVariance>(item, item)).size, "views")
    }
}
