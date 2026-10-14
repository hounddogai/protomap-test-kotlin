package com.acme.transport.customs.hold.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldCycle("code-893", "label-566")
        val view = toHoldCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldCycleViews(mutableListOf<HoldCycle>(item, item)).size, "views")
    }
}
