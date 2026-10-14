package com.acme.transport.safety.coaching.coachingscore

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingCoachingScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingCoachingScore("code-908", "name-276")
        val view = toRollingCoachingScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingCoachingScoreViews(mutableListOf<RollingCoachingScore>(item, item)).size, "views")
    }
}
