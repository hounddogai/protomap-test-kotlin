package com.acme.transport.customs.hold.holdallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldMetric("code-692", "name-273")
        val view = toHoldMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldMetricViews(mutableListOf<HoldMetric>(item, item)).size, "views")
    }
}
