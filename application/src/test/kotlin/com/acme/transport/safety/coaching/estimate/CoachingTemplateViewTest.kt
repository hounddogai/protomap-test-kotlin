package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingTemplate("code-586", "label-148")
        val view = toCoachingTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingTemplateViews(mutableListOf<CoachingTemplate>(item, item)).size, "views")
    }
}
