package com.acme.transport.safety.coaching.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedCoachingTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedCoachingTemplate("code-554", "name-641")
        val view = toWeightedCoachingTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedCoachingTemplateViews(mutableListOf<WeightedCoachingTemplate>(item, item)).size, "views")
    }
}
