package com.acme.transport.terminal.crossdock.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingCrossDockSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingCrossDockSummary("code-973", "title-938")
        val view = toRollingCrossDockSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingCrossDockSummaryViews(mutableListOf<RollingCrossDockSummary>(item, item)).size, "views")
    }
}
