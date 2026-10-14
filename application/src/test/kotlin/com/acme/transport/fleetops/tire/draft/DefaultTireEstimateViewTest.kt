package com.acme.transport.fleetops.tire.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTireEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTireEstimate("code-888", "name-965")
        val view = toDefaultTireEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTireEstimateViews(mutableListOf<DefaultTireEstimate>(item, item)).size, "views")
    }
}
