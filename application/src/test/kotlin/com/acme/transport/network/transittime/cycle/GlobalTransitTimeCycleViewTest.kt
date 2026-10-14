package com.acme.transport.network.transittime.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTransitTimeCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalTransitTimeCycle("code-937", "label-882")
        val view = toGlobalTransitTimeCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalTransitTimeCycleViews(mutableListOf<GlobalTransitTimeCycle>(item, item)).size, "views")
    }
}
