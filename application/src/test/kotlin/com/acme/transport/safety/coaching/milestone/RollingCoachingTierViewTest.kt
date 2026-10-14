package com.acme.transport.safety.coaching.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingCoachingTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingCoachingTier("code-421", "name-498")
        val view = toRollingCoachingTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingCoachingTierViews(mutableListOf<RollingCoachingTier>(item, item)).size, "views")
    }
}
