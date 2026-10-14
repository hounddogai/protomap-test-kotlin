package com.acme.transport.safety.coaching.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultCoachingBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultCoachingBaseline("code-327", "name-931")
        val view = toDefaultCoachingBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultCoachingBaselineViews(mutableListOf<DefaultCoachingBaseline>(item, item)).size, "views")
    }
}
