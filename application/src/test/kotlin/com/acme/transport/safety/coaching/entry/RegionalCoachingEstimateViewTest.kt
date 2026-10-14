package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCoachingEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCoachingEstimate("code-77", "name-4")
        val view = toRegionalCoachingEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCoachingEstimateViews(mutableListOf<RegionalCoachingEstimate>(item, item)).size, "views")
    }
}
