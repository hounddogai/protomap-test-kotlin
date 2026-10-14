package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalViolationQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalViolationQuota("code-112", "label-363")
        val view = toSeasonalViolationQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalViolationQuotaViews(mutableListOf<SeasonalViolationQuota>(item, item)).size, "views")
    }
}
