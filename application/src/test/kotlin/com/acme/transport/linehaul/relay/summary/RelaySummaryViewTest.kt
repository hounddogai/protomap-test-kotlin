package com.acme.transport.linehaul.relay.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class RelaySummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRelaySummary("code-557", "name-561")
        val view = toRelaySummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRelaySummaryViews(mutableListOf<RelaySummary>(item, item)).size, "views")
    }
}
