package com.acme.transport.emissions.routeoffset.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictRouteOffsetThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictRouteOffsetThreshold("code-22", "title-710")
        val view = toStrictRouteOffsetThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictRouteOffsetThresholdViews(mutableListOf<StrictRouteOffsetThreshold>(item, item)).size, "views")
    }
}
