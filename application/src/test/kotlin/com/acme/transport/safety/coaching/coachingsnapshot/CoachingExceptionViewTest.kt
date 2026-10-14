package com.acme.transport.safety.coaching.coachingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingException("code-335", "label-695")
        val view = toCoachingExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingExceptionViews(mutableListOf<CoachingException>(item, item)).size, "views")
    }
}
