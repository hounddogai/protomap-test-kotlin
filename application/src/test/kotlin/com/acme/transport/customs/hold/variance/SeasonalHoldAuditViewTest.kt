package com.acme.transport.customs.hold.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalHoldAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalHoldAudit("code-39", "title-777")
        val view = toSeasonalHoldAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalHoldAuditViews(mutableListOf<SeasonalHoldAudit>(item, item)).size, "views")
    }
}
