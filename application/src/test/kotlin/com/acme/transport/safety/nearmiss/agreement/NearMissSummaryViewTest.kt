package com.acme.transport.safety.nearmiss.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissSummary("code-343", "name-300")
        val view = toNearMissSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissSummaryViews(mutableListOf<NearMissSummary>(item, item)).size, "views")
    }
}
