package com.acme.transport.safety.coaching.target

import kotlin.test.Test
import kotlin.test.assertEquals

class CoachingTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCoachingTarget("code-336", "title-852")
        val view = toCoachingTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCoachingTargetViews(mutableListOf<CoachingTarget>(item, item)).size, "views")
    }
}
