package com.acme.transport.safety.coaching.coachingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingForecast("code-737", "title-634")
        val view = toCoachingForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingForecastViews(mutableListOf<CoachingForecast>(item, item)).size, "views")
    }
}
