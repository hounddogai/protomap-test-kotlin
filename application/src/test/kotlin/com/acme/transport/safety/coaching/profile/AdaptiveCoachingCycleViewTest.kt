package com.acme.transport.safety.coaching.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCoachingCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCoachingCycle("code-645", "name-809")
        val view = toAdaptiveCoachingCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCoachingCycleViews(mutableListOf<AdaptiveCoachingCycle>(item, item)).size, "views")
    }
}
