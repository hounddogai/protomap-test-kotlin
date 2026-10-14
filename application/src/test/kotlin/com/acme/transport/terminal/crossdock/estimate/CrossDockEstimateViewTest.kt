package com.acme.transport.terminal.crossdock.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockEstimate("code-579", "title-523")
        val view = toCrossDockEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockEstimateViews(mutableListOf<CrossDockEstimate>(item, item)).size, "views")
    }
}
