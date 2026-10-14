package com.acme.transport.customs.hold.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldSummary("code-49", "name-451")
        val view = toHoldSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldSummaryViews(mutableListOf<HoldSummary>(item, item)).size, "views")
    }
}
