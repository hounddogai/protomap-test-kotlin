package com.acme.transport.safety.cameraevent.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalCameraEventPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalCameraEventPlan("code-214", "title-873")
        val view = toSeasonalCameraEventPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalCameraEventPlanViews(mutableListOf<SeasonalCameraEventPlan>(item, item)).size, "views")
    }
}
