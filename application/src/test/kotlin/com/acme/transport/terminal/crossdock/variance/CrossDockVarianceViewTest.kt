package com.acme.transport.terminal.crossdock.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockVariance("code-98", "name-619")
        val view = toCrossDockVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockVarianceViews(mutableListOf<CrossDockVariance>(item, item)).size, "views")
    }
}
