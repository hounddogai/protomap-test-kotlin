package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictParcelCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictParcelCycle("code-972", "name-643")
        val view = toStrictParcelCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictParcelCycleViews(mutableListOf<StrictParcelCycle>(item, item)).size, "views")
    }
}
