package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictServiceAreaCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictServiceAreaCycle("code-793", "title-379")
        val view = toStrictServiceAreaCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictServiceAreaCycleViews(mutableListOf<StrictServiceAreaCycle>(item, item)).size, "views")
    }
}
