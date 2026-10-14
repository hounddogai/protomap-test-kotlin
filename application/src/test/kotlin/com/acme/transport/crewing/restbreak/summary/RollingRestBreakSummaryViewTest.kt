package com.acme.transport.crewing.restbreak.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRestBreakSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRestBreakSummary("code-443", "name-142")
        val view = toRollingRestBreakSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRestBreakSummaryViews(mutableListOf<RollingRestBreakSummary>(item, item)).size, "views")
    }
}
