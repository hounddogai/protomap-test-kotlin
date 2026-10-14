package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalRedeliveryAuditViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalRedeliveryAudit("code-224", "label-403")
        val view = toSeasonalRedeliveryAuditView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalRedeliveryAuditViews(mutableListOf<SeasonalRedeliveryAudit>(item, item)).size, "views")
    }
}
