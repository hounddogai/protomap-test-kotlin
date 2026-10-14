package com.acme.transport.safety.nearmiss.misscycle

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultNearMissCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultNearMissCycle("code-63", "name-142")
        val view = toDefaultNearMissCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultNearMissCycleViews(mutableListOf<DefaultNearMissCycle>(item, item)).size, "views")
    }
}
