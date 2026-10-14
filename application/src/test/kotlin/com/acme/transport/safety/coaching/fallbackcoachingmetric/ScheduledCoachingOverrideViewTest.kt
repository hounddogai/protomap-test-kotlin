package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledCoachingOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledCoachingOverride("code-591", "name-114")
        val view = toScheduledCoachingOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledCoachingOverrideViews(mutableListOf<ScheduledCoachingOverride>(item, item)).size, "views")
    }
}
