package com.acme.transport.customs.clearance.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientClearanceMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientClearanceMetric("code-216", "title-203")
        val view = toLenientClearanceMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientClearanceMetricViews(mutableListOf<LenientClearanceMetric>(item, item)).size, "views")
    }
}
