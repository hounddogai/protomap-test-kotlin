package com.acme.transport.safety.scorecard.tieredscorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardQuota("code-951", "label-947")
        val view = toScorecardQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardQuotaViews(mutableListOf<ScorecardQuota>(item, item)).size, "views")
    }
}
