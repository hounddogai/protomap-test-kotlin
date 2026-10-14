package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffCycle("code-556", "label-527")
        val view = toCutoffCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffCycleViews(mutableListOf<CutoffCycle>(item, item)).size, "views")
    }
}
